package com.tty.ari.tool;

import com.tty.ari.Ari;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LastDamageTracker {

    private final Map<Entity, List<DamageRecord>> records = new ConcurrentHashMap<>();
    /**
     * 实体之间的伤害记录（玩家 - 玩家，玩家 - 实体， 实体 - 玩家， 实体 x 实体）
     * @param timestamp 造成伤害的时间戳
     * @param damager 伤害着
     * @param damage 造成的伤害
     * @param location 位置
     * @param weapon 武器
     */
    public record DamageRecord(long timestamp, Entity damager, double damage, Location location, ItemStack weapon) {}

    public List<DamageRecord> getRecords(Entity victim) {
        List<DamageRecord> list = this.records.get(victim);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 清除与被害者所有有关的记录
     * @param victim 被害者
     */
    public void clearRecords(Entity victim) {
        this.records.remove(victim);
        Iterator<Map.Entry<Entity, List<DamageRecord>>> it = this.records.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, List<DamageRecord>> entry = it.next();
            List<DamageRecord> list = entry.getValue();
            list.removeIf(r -> {
                Entity damager = r.damager();
                return damager != null && damager.equals(victim);
            });
            if (list.isEmpty()) {
                it.remove();
            }
        }
    }

    public void removeAll() {
        this.records.clear();
    }

    /**
     * 返回当前 tracker 中所有被记录的受害者实体的快照。
     */
    public Set<Entity> getVictimsSnapshot() {
        return new HashSet<>(this.records.keySet());
    }

    /**
     * 返回 victim 的最近一次记录时间戳（毫秒）。若没有记录，返回 0。
     */
    public long getLastTimestamp(Entity victim) {
        List<DamageRecord> list = this.records.get(victim);
        if (list == null || list.isEmpty()) return 0L;
        return list.getLast().timestamp();
    }

    private boolean checkInvolvesPlayer(Entity victim, Entity attacker, Entity directEntity) {
        // 攻击者是玩家
        if (attacker instanceof Player) {
            return true;
        }

        // 受害者是玩家
        if (victim instanceof Player) {
            return true;
        }

        // 弹射物发射者是玩家
        if (directEntity instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                return true;
            }
        }

        // 喷溅药水发射者是玩家
        if (directEntity instanceof ThrownPotion potion) {
            if (potion.getShooter() instanceof Player) {
                return true;
            }
        }

        // 药水云来源是玩家
        if (directEntity instanceof AreaEffectCloud cloud) {
            return cloud.getSource() instanceof Player;
        }

        return false;
    }

    /**
     * 查找武器
     * @param attacker 攻击者
     * @param directEntity 直接攻击的实体类型
     * @return 武器
     */
    @Nullable
    private ItemStack getWeapon(@Nullable Entity attacker, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        ItemStack weapon = null;

        if (directEntity != null && Bukkit.isOwnedByCurrentRegion(directEntity)) {
            if (directEntity instanceof ThrownPotion potion) {
                weapon = potion.getItem();
            } else if (directEntity instanceof AreaEffectCloud) {
                if (causingEntity instanceof ThrownPotion potion && Bukkit.isOwnedByCurrentRegion(causingEntity)) {
                    weapon = potion.getItem();
                }
                if (weapon == null && attacker instanceof LivingEntity living && Bukkit.isOwnedByCurrentRegion(attacker)) {
                    EntityEquipment eq = living.getEquipment();
                    if (eq != null) weapon = eq.getItemInMainHand();
                }
            } else {
                switch (directEntity) {
                    case LivingEntity living -> {
                        if (Bukkit.isOwnedByCurrentRegion(living)) {
                            EntityEquipment eq = living.getEquipment();
                            if (eq != null) weapon = eq.getItemInMainHand();
                        }
                    }
                    case Item dropped -> weapon = dropped.getItemStack();
                    case Projectile projectile -> {
                        if (projectile.getShooter() instanceof LivingEntity shooter && Bukkit.isOwnedByCurrentRegion(shooter)) {
                            EntityEquipment equipment = shooter.getEquipment();
                            if (equipment != null) weapon = equipment.getItemInMainHand();
                        }
                        if (weapon == null && projectile instanceof Trident trident) {
                            weapon = trident.getItemStack();
                        }
                    }
                    default -> {}
                }
            }
        }

        if (weapon == null && attacker instanceof LivingEntity living && Bukkit.isOwnedByCurrentRegion(attacker)) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) weapon = eq.getItemInMainHand();
        }
        return weapon;
    }

    /**
     * 判断是否为间接伤害
     */
    private boolean isIndirectDamage(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case MAGIC, POISON, WITHER, THORNS, FIRE_TICK, FIRE, LIGHTNING, SONIC_BOOM, CUSTOM, FALL, FALLING_BLOCK, FREEZE, FLY_INTO_WALL, BLOCK_EXPLOSION, ENTITY_EXPLOSION -> true;
            default -> false;
        };
    }

    public void addRecord(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Damageable victim)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        DamageSource damageSource = event.getDamageSource();
        Entity causingEntity = damageSource.getCausingEntity();
        Entity directEntity = damageSource.getDirectEntity();

        Entity resolvedAttacker = causingEntity;

        // 喷溅药水
        if (directEntity instanceof ThrownPotion || causingEntity instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) (directEntity instanceof ThrownPotion ? directEntity : causingEntity);
            if (potion.getShooter() instanceof Entity shooter) {
                resolvedAttacker = shooter;
            }
        }
        // 药水效果云
        if (directEntity instanceof AreaEffectCloud cloud) {
            if (cloud.getSource() instanceof Entity source) {
                resolvedAttacker = source;
            } else {
                List<DamageRecord> records = this.getRecords(victim);
                if (!records.isEmpty()) {
                    DamageRecord lastRecord = records.getLast();
                    long timeDiff = System.currentTimeMillis() - lastRecord.timestamp();
                    if (timeDiff < 2000) {
                        resolvedAttacker = lastRecord.damager();
                    }
                }
            }
        }
        // 弹射物
        if (directEntity instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter) {
                resolvedAttacker = shooter;
            }
        }

        if (directEntity instanceof TNTPrimed tnt) {
            resolvedAttacker = (tnt.getSource() instanceof Entity source) ? source : tnt;
        } else if (causingEntity instanceof TNTPrimed tnt) {
            resolvedAttacker = (tnt.getSource() instanceof Entity source) ? source : tnt;
        } else if (directEntity instanceof ExplosiveMinecart minecart) {
            resolvedAttacker = minecart;
        } else if (causingEntity instanceof ExplosiveMinecart minecart) {
            resolvedAttacker = minecart;
        }

        if (resolvedAttacker instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter) {
                resolvedAttacker = shooter;
            } else {
                resolvedAttacker = directEntity;
            }
        }

        boolean isIndirectDamage = this.isIndirectDamage(event.getCause());

        if (isIndirectDamage
                && !(resolvedAttacker instanceof LivingEntity)
                && !(resolvedAttacker instanceof TNTPrimed)
                && !(resolvedAttacker instanceof ExplosiveMinecart)) {

            List<DamageRecord> records = this.getRecords(victim);
            if (!records.isEmpty()) {
                DamageRecord lastRecord = records.getLast();
                long timeDiff = System.currentTimeMillis() - lastRecord.timestamp();
                long threshold = switch (cause) {
                    case MAGIC, POISON, WITHER -> 1500L;
                    case THORNS, LIGHTNING, SONIC_BOOM, CUSTOM -> 1000L;
                    case FIRE_TICK, FIRE, FALL, FALLING_BLOCK, FREEZE -> 5000L;
                    case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> 3000L;
                    default -> 2000L;
                };
                if (timeDiff < threshold) {
                    resolvedAttacker = lastRecord.damager();
                    this.log(victim, cause, timeDiff, threshold, resolvedAttacker);
                }
            }
        } else {
            this.log(victim, cause, null, null, resolvedAttacker);
        }

        if (!this.checkInvolvesPlayer(victim, resolvedAttacker, directEntity)) return;

        this.records.computeIfAbsent(victim, k -> new CopyOnWriteArrayList<>())
                .add(new DamageRecord(System.currentTimeMillis(), resolvedAttacker, event.getFinalDamage(), victim.getLocation(),
                        this.getWeapon(resolvedAttacker, directEntity, causingEntity)));
    }

    private void log(Entity victim, EntityDamageEvent.DamageCause cause, Long timeDiff, Long threshold, Entity attacker) {
        if (victim == null) return;
        Ari.instance.getScheduler().runAtEntity(victim, i -> {
            Location location = victim.getLocation();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            String victimName = victim.getName();
            if (attacker == null) return;
            Ari.instance.getScheduler().runAtEntity(attacker, t ->
                Ari.instance.getLog().debug("victim: {}, damage_type: {}, time_difference: {} ms (threshold: {} ms), attacker: {}, location: x: {}, y: {}, z: {}",
                    victimName,
                    cause.name(),
                    timeDiff,
                    threshold,
                    attacker.getName(),
                    x,
                    y,
                    z
                ), null);
        }, null);
    }

}
