package com.tty.tool;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.api.Log;
import com.tty.api.PublicFunctionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.*;

import static com.tty.listener.DamageTrackerListener.DAMAGE_TRACKER;


public class PlayerDeathInfoCollector {

    private static final double RANGED_ESCAPE_DISTANCE = 20.0;
    private static final double MELEE_ESCAPE_DISTANCE = 5.0;

    private static final Set<EntityDamageEvent.DamageCause> DIRECT_COMBAT_CAUSES = Set.of(
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
            EntityDamageEvent.DamageCause.PROJECTILE
    );

    public static class DeathInfo {

        public Player victim;
        public long deathTime;
        public EntityDamageEvent.DamageCause deathCause;
        public boolean isProjectile;
        public Entity killer;
        public ItemStack weapon;
        public boolean isEscapeAttempt;
        public boolean isDestine;

        @Override
        public String toString() {
            return "DeathInfo{" +
                    "victim=" + victim.getName() +
                    ", deathTime=" + deathTime +
                    ", deathCause=" + deathCause +
                    ", killer=" + (killer != null ? killer.getType().name() : "null") +
                    ", weapon=" + (weapon != null ? weapon.getType().name() : "null") +
                    ", isEscapeAttempt=" + isEscapeAttempt +
                    ", isDestine=" + isDestine +
                    '}';
        }

        public String getRandomOfList(String keyPath, boolean isDestine) {
            String killerName = this.killer == null ? null : this.killer.getType().name().toLowerCase();
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> pool = new ArrayList<>();
            List<String> resolvedKeys = new ArrayList<>();
            String basePath = isDestine ? keyPath + ".destine" : keyPath;
            findMessages(basePath, killerName, type, pool, resolvedKeys);
            Log.debug("DeathMessage resolved keys: {}", String.join(", ", resolvedKeys));
            return pool.isEmpty() ? "" : pool.get(PublicFunctionUtils.randomGenerator(0, pool.size() - 1));
        }

        private void findMessages(String basePath, String killerName, Type type, List<String> pool, List<String> resolvedKeys) {
            if (killerName != null) {
                String killerKey = basePath + "." + killerName;
                List<String> killerList = Ari.C_INSTANCE.getValue(killerKey, FilePath.DEATH_MESSAGE, type, null);
                if (killerList != null && !killerList.isEmpty()) {
                    pool.addAll(killerList);
                    resolvedKeys.add(killerKey);
                    return;
                }
            }
            String publicKey = basePath + ".public";
            List<String> publicList = Ari.C_INSTANCE.getValue(publicKey, FilePath.DEATH_MESSAGE, type, null);
            if (publicList != null && !publicList.isEmpty()) {
                pool.addAll(publicList);
                resolvedKeys.add(publicKey);
            }
        }

        public String getRandomOfList(String keyPath) {
            return getRandomOfList(keyPath, this.isDestine);
        }
    }

    public DeathInfo collect(PlayerDeathEvent event) {
        DeathInfo info = new DeathInfo();
        info.victim = event.getEntity();
        info.deathTime = System.currentTimeMillis();

        EntityDamageEvent lastDamage = info.victim.getLastDamageCause();
        info.deathCause = lastDamage != null ? lastDamage.getCause() : EntityDamageEvent.DamageCause.CUSTOM;
        info.isProjectile = info.deathCause == EntityDamageEvent.DamageCause.PROJECTILE;
        info.killer = null;
        info.weapon = null;
        info.isEscapeAttempt = false;
        info.isDestine = false;

        Log.debug("death cause resolved: {}", info.deathCause);

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(info.victim);
        Log.debug("damage records count: {}", records.size());

        if (!records.isEmpty()) {
            Entity firstAttacker = null;
            Entity lastAttacker = null;
            ItemStack lastWeapon = null;
            Location firstAttackLocation = null;

            // 遍历所有记录，寻找第一个攻击者和最后一个攻击者
            for (LastDamageTracker.DamageRecord r : records) {
                Entity damager = r.damager();
                if (damager == null) continue;

                Entity actual = resolveAttacker(damager);
                if (actual == null) continue;

                // 记录第一个攻击者
                if (firstAttacker == null) {
                    firstAttacker = actual;
                    firstAttackLocation = r.location();
                    Log.debug("first attacker resolved: {}", actual.getType().name());
                }

                // 更新最后一个攻击者
                lastAttacker = actual;
                lastWeapon = r.weapon();
                Log.debug("last attacker updated: {}", actual.getType().name());
            }

            if (lastAttacker != null) {
                info.killer = lastAttacker;
                info.weapon = lastWeapon;

                // 首先判断是否试图逃跑
                info.isEscapeAttempt = this.evaluateEscape(info.victim, info.killer, firstAttackLocation, info.victim.getLocation(), info.deathCause);

                // 判断是否为"注定"死亡
                info.isDestine = this.determineIfDestine(records, info.victim, firstAttacker, lastAttacker, info.deathCause);

                Log.debug("combat analysis success, killer: {}, weapon: {}", info.killer.getType().name(), info.weapon != null ? info.weapon.getType().name() : "null");
                Log.debug("escape attempt: {}, destine: {}", info.isEscapeAttempt, info.isDestine);

                // 后备武器获取
                if (info.weapon == null && info.killer instanceof LivingEntity living) {
                    EntityEquipment eq = living.getEquipment();
                    if (eq != null) {
                        info.weapon = eq.getItemInMainHand();
                        Log.debug("weapon retrieved from killer's equipment: {}", info.weapon.getType().name());
                    }
                }

                return info;
            }
        }

        //从 DamageSource 获取信息
        DamageSource source = event.getDamageSource();
        info.killer = source.getCausingEntity();

        if (info.killer instanceof LivingEntity e) {
            EntityEquipment eq = e.getEquipment();
            info.weapon = eq == null ? null : eq.getItemInMainHand();
        }

        //无法判断是否为"注定"，默认为false
        Log.debug("fallback analysis used, killer: {}",
                info.killer != null ? info.killer.getType().name() : "null");

        return info;
    }

    /**
     * 判断是否为"注定"死亡
     * 1. 玩家试图逃跑但被杀死（isEscapeAttempt为true）
     * 2. 玩家被多个不同攻击者围攻致死
     * 3. 玩家死于间接伤害，但之前曾被其他攻击者攻击
     * 4. 如果玩家被直接秒杀，不算注定
     */
    private boolean determineIfDestine(List<LastDamageTracker.DamageRecord> records,
                                       Entity victim, Entity firstAttacker, Entity lastAttacker,
                                       EntityDamageEvent.DamageCause deathCause) {
        
        if (records.size() == 1) {
            LastDamageTracker.DamageRecord last = records.getLast();
            if (last != null && victim instanceof Damageable && victim instanceof Attributable attributable) {
                double damage = last.damage();
                AttributeInstance attribute = attributable.getAttribute(Attribute.MAX_HEALTH);
                if (attribute != null) {
                    double value = attribute.getValue();
                    if (damage >= value) {
                        Log.debug("destine: instant kill, not destine");
                        return false;
                    }
                }
            }
        }

        // 统计不同的攻击者数量
        Set<Entity> uniqueAttackers = new HashSet<>();
        for (LastDamageTracker.DamageRecord record : records) {
            Entity damager = record.damager();
            if (damager == null) continue;
            Entity attacker = this.resolveAttacker(damager);
            if (attacker != null) {
                uniqueAttackers.add(attacker);
            }
        }

        // 如果有多个不同的攻击者，就是"注定"（被围攻）
        if (uniqueAttackers.size() > 1) {
            Log.debug("destine: attacked by {} unique attackers", uniqueAttackers.size());
            return true;
        }

        // 如果是间接伤害死亡
        boolean isIndirectDamage = this.isIndirectDamageCause(deathCause);
        if (isIndirectDamage) {
            if (firstAttacker != null && lastAttacker != null) {
                Log.debug("destine: indirect damage from different attacker");
                return true;
            }
        }

        // 其他情况：非"注定"
        Log.debug("destine: not destine (direct combat, single attacker)");
        return false;
    }

    private boolean evaluateEscape(Entity victim, Entity killer, Location firstAttackLocation, Location deathLocation,
                                   EntityDamageEvent.DamageCause cause) {

        if (victim.equals(killer)) return false;

        if (firstAttackLocation == null || deathLocation == null) {
            Log.debug("escape check skipped: location missing");
            return false;
        }

        if (!Objects.equals(firstAttackLocation.getWorld(), deathLocation.getWorld())) {
            Log.debug("escape check skipped: different worlds");
            return false;
        }

        // 根据伤害类型调整距离阈值
        double escapeDistance;
        if (DIRECT_COMBAT_CAUSES.contains(cause)) {
            // 直接战斗：远程或近战
            escapeDistance = cause == EntityDamageEvent.DamageCause.PROJECTILE ? RANGED_ESCAPE_DISTANCE : MELEE_ESCAPE_DISTANCE;
        } else {
            // 间接伤害：使用中等阈值
            escapeDistance = 15.0;
        }

        double sqDist = firstAttackLocation.distanceSquared(deathLocation);
        double threshold = escapeDistance * escapeDistance;
        boolean escaped = sqDist > threshold;

        Log.debug("escape evaluated: sqDist={}, threshold={}, escaped={}, cause={}", sqDist, threshold, escaped, cause.name());
        return escaped;
    }

    private Entity resolveAttacker(Entity damager) {
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter) return shooter;
            UUID owner = projectile.getOwnerUniqueId();
            if (owner != null) return Bukkit.getPlayer(owner);
        }
        return damager;
    }

    /**
     * 判断是否为间接伤害原因
     */
    private boolean isIndirectDamageCause(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case MAGIC, POISON, WITHER, THORNS, FIRE_TICK, FIRE, LIGHTNING, SONIC_BOOM,
                 CUSTOM, FALL, FALLING_BLOCK, FREEZE, FLY_INTO_WALL,
                 BLOCK_EXPLOSION, ENTITY_EXPLOSION -> true;
            default -> false;
        };
    }
}