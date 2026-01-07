package com.tty.tool;

import com.tty.lib.Log;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LastDamageTracker {

    /**
     * key: 受害者
     * value: 被其攻击的列表（按时间顺序，越后面的条目越新）
     */
    private final Map<Entity, List<DamageRecord>> records = new HashMap<>();

    /**
     * 实体之间的伤害记录（玩家 - 玩家，玩家 - 实体， 实体 - 玩家， 实体 x 实体）
     * @param timestamp 造成伤害的时间戳
     * @param damager 伤害着
     * @param damage 造成的伤害
     * @param location 位置
     * @param weapon 武器
     */
    public record DamageRecord(long timestamp, Entity damager, double damage, Location location, ItemStack weapon) {}

    public void addRecord(Entity victim, Entity damager, double damage, ItemStack weapon) {
        synchronized (this) {
            this.records.computeIfAbsent(victim, k -> new ArrayList<>())
                    .add(new DamageRecord(System.currentTimeMillis(), damager, damage, victim.getLocation(), weapon));
        }
    }

    public List<DamageRecord> getRecords(Entity victim) {
        synchronized (this) {
            List<DamageRecord> list = this.records.get(victim);
            return list == null ? Collections.emptyList() : list;
        }
    }

    /**
     * 清除与被害者所有有关的记录
     * @param victim 被害者
     */
    public void clearRecords(Entity victim) {
        synchronized (this) {
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
    }

    public void removeAll() {
        synchronized (this) {
            this.records.clear();
        }
    }

    /**
     * 返回当前 tracker 中所有被记录的受害者实体的快照。
     */
    public Set<Entity> getVictimsSnapshot() {
        synchronized (this) {
            return new HashSet<>(this.records.keySet());
        }
    }

    /**
     * 返回 victim 的最近一次记录时间戳（毫秒）。若没有记录，返回 0。
     */
    public long getLastTimestamp(Entity victim) {
        synchronized (this) {
            List<DamageRecord> list = this.records.get(victim);
            if (list == null || list.isEmpty()) return 0L;
            return list.getLast().timestamp();
        }
    }

    public AttackCheckResult checkAttackInvolvesPlayer(Entity victim, Entity causingEntity, Entity directEntity, EntityDamageEvent.DamageCause cause) {
        Entity resolvedAttacker = causingEntity;
        //喷溅药水
        if (directEntity instanceof ThrownPotion || causingEntity instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) (directEntity instanceof ThrownPotion ? directEntity : causingEntity);
            if (potion.getShooter() instanceof Entity shooter) {
                resolvedAttacker = shooter;
            }
        }
        //药水效果云
        if (directEntity instanceof AreaEffectCloud cloud) {
            if (cloud.getSource() instanceof Entity source) {
                resolvedAttacker = source;
            } else {
                List<DamageRecord> records = this.getRecords(victim);
                if (!records.isEmpty()) {
                    DamageRecord lastRecord = records.getLast();
                    long timeDiff = System.currentTimeMillis() - lastRecord.timestamp();
                    if (timeDiff < 2000) { //回溯2秒前的攻击者
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
        if (resolvedAttacker instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter) {
                resolvedAttacker = shooter;
            } else {
                resolvedAttacker = directEntity;
            }
        }

        boolean isIndirectDamage = this.isIndirectDamage(cause);
        if (isIndirectDamage && !(resolvedAttacker instanceof Player)) {
            List<DamageRecord> records = this.getRecords(victim);
            if (!records.isEmpty()) {
                DamageRecord lastRecord = records.getLast();
                long timeDiff = System.currentTimeMillis() - lastRecord.timestamp();
                // 间接伤害
                long threshold = switch (cause) {
                    case MAGIC, POISON, WITHER -> 1500L; // 1.5秒，药水效果持续时间
                    case THORNS -> 1000L; // 1秒，荆棘伤害立即反弹
                    case FIRE_TICK, FIRE -> 3000L; // 3秒，火焰可能持续燃烧
                    case LIGHTNING, SONIC_BOOM, CUSTOM -> 1000L; // 1秒内被雷劈死或被音爆或者是自定义
                    case FALL, FALLING_BLOCK, FREEZE -> 4000L; // 4秒内的摔死、被砸死和冻死
                    case FLY_INTO_WALL -> 2000L; //速度太快撞死
                    case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> 3000L; // 3秒内的炸死
                    default -> 2000L; // 2秒默认阈值
                };

                if (timeDiff < threshold) {
                    resolvedAttacker = lastRecord.damager();
                    this.log(victim, cause, timeDiff, threshold, resolvedAttacker);
                }
            }
        } else {
            this.log(victim, cause, null, null, resolvedAttacker);
        }

        //检查是否涉及玩家
        boolean involvesPlayer = this.checkInvolvesPlayer(victim, resolvedAttacker, directEntity, cause);
        Log.debug("[CHECK_RESULT] victim=%s, involvesPlayer=%s, resolvedAttacker=%s",
                victim != null ? victim.getType().name() : "null",
                involvesPlayer,
                resolvedAttacker != null ? resolvedAttacker.getType().name() : "null");
        return new AttackCheckResult(involvesPlayer, resolvedAttacker);
    }

    private boolean checkInvolvesPlayer(Entity victim, Entity attacker, Entity directEntity, EntityDamageEvent.DamageCause cause) {
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
    public ItemStack getWeapon(@Nullable Entity attacker, @Nullable Entity directEntity, @Nullable Entity causingEntity, EntityDamageEvent.DamageCause cause) {
        ItemStack weapon = null;

        //对于喷溅药水，武器是药水瓶本身
        if (directEntity instanceof ThrownPotion potion) {
            weapon = potion.getItem();
        }
        //对于药水云，尝试获取来源的药水瓶
        else if (directEntity instanceof AreaEffectCloud cloud) {
            // 药水云可能由药水瓶产生
            if (causingEntity instanceof ThrownPotion potion) {
                weapon = potion.getItem();
            }
        }
        //直接实体获取武器
        else if (directEntity != null) {
            switch (directEntity) {
                case LivingEntity living -> {
                    EntityEquipment eq = living.getEquipment();
                    if (eq != null) {
                        weapon = eq.getItemInMainHand();
                    }
                }
                case Item dropped -> weapon = dropped.getItemStack();
                case Projectile projectile -> {
                    if (projectile.getShooter() instanceof LivingEntity shooter) {
                        EntityEquipment equipment = shooter.getEquipment();
                        if (equipment != null) {
                            weapon = equipment.getItemInMainHand();
                        }
                    }
                }
                default -> { }
            }
        }
        if (weapon == null && attacker instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) {
                weapon = eq.getItemInMainHand();
            }
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

    private void log(Entity victim, EntityDamageEvent.DamageCause cause, Long timeDiff, Long threshold, Entity attacker) {
        Log.debug("[RECORD_BEFORE_CHECK] victim: %s, damage_type: %s, time_difference: %s ms (threshold: %s ms), attacker: %s, location: %s",
                victim.getName(),
                cause.name(),
                timeDiff,
                threshold,
                attacker != null ? attacker.getName() : "null",
                victim.getLocation().toString()
        );
    }

    public record AttackCheckResult(boolean involvesPlayer, Entity resolvedAttacker) {}

}
