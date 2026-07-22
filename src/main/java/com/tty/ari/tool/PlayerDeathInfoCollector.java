package com.tty.ari.tool;

import com.google.common.reflect.TypeToken;
import com.tty.api.scheduler.RunTask;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.lang.DeathMessageLang;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.tty.ari.listener.DamageTrackerListener.DAMAGE_TRACKER;


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
            this.findMessages(basePath, killerName, type, pool, resolvedKeys);
            Ari.instance.getLog().debug("death message resolved keys: {}", String.join(", ", resolvedKeys));
            if (pool.isEmpty()) {
                Ari.instance.getLog().warn("no death message found for keyPath: {}, isDestine: {}, basePath: {}, killerName: {}", keyPath, isDestine, basePath, killerName);
                String killerKey = killerName != null ? basePath + "." + killerName : "null";
                String publicKey = basePath + ".public";
                Ari.instance.getLog().warn("attempted keys: {} and {}", killerKey, publicKey);
            }
            return pool.isEmpty() ? "" : pool.get(PublicFunctionUtils.randomGenerator(0, pool.size() - 1));
        }

        private void findMessages(String basePath, String killerName, Type type, List<String> pool, List<String> resolvedKeys) {
            if (killerName != null) {
                String killerKey = basePath + "." + killerName;
                List<String> killerList = Ari.instance.getConfigurationManager().get(DeathMessageLang.class).getValue(killerKey, type, null);
                if (killerList != null && !killerList.isEmpty()) {
                    pool.addAll(killerList);
                    resolvedKeys.add(killerKey);
                    return;
                }
            }
            String publicKey = basePath + ".public";
            List<String> publicList = Ari.instance.getConfigurationManager().get(DeathMessageLang.class).getValue(publicKey, type, null);
            if (publicList != null && !publicList.isEmpty()) {
                pool.addAll(publicList);
                resolvedKeys.add(publicKey);
            }
        }

        public String getRandomOfList(String keyPath) {
            return this.getRandomOfList(keyPath, this.isDestine);
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

        Ari.instance.getLog().debug("death cause resolved: {}", info.deathCause);

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(info.victim);
        Ari.instance.getLog().debug("damage records count: {}", records.size());

        if (records.isEmpty()) {
            info.killer = event.getDamageSource().getCausingEntity();
            if (info.killer instanceof LivingEntity e) {
                EntityEquipment eq = e.getEquipment();
                info.weapon = eq == null ? null : eq.getItemInMainHand();
            }
            Ari.instance.getLog().debug("fallback analysis used, killer: {}", info.killer != null ? info.killer.getType().name() : "null");
            return info;
        }

        List<Entity> resolvedAttackers = new ArrayList<>();
        Entity first = null;
        Entity last = null;
        ItemStack weapon = null;
        Location firstLocation = null;

        for (LastDamageTracker.DamageRecord record : records) {

            Entity actual = null;
            try {
                actual = this.resolveAttacker(record.damager()).get(20, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                Ari.instance.getLog().error(e);
            }
            if (actual == null) continue;

            resolvedAttackers.add(actual);
            if (first == null) {
                first = actual;
                firstLocation = record.location();
            }
            last = actual;
            weapon = record.weapon();
        }

        if (last != null) {
            info.killer = last;
            info.weapon = weapon;
            info.isEscapeAttempt = this.evaluateEscape(info.victim, info.killer, firstLocation, info.victim.getLocation(), info.deathCause);
            info.isDestine = this.determineIfDestine(records, info.victim, info.deathCause, resolvedAttackers);
            if (info.weapon == null && info.killer instanceof LivingEntity living) {
                EntityEquipment eq = living.getEquipment();
                if (eq != null) {
                    info.weapon = eq.getItemInMainHand();
                }
            }
            Ari.instance.getLog().debug("combat analysis success, killer: {}, weapon: {}", info.killer.getType().name(), info.weapon != null ? info.weapon.getType().name() : "null");
        } else {
            info.killer = event.getDamageSource().getCausingEntity();
            if (info.killer instanceof LivingEntity e) {
                EntityEquipment eq = e.getEquipment();
                info.weapon = eq == null ? null : eq.getItemInMainHand();
            }
            Ari.instance.getLog().debug("fallback analysis used (no valid attacker)");
        }

        Ari.instance.getLog().debug("escape attempt: {}, destine: {}", info.isEscapeAttempt, info.isDestine);
        return info;
    }

    /**
     * 判断是否为注定死亡
     * 玩家试图逃跑但被杀死
     * 玩家被多个不同攻击者围攻致死
     * 玩家死于间接伤害，但之前曾被其他攻击者攻击
     * 如果玩家被直接秒杀，不算注定
     */
    private boolean determineIfDestine(List<LastDamageTracker.DamageRecord> records, Entity victim, EntityDamageEvent.DamageCause deathCause, List<Entity> resolvedAttackers) {

        if (records.size() == 1) {
            LastDamageTracker.DamageRecord lastRecord = records.get(records.size() - 1);
            if (lastRecord != null && victim instanceof Damageable && victim instanceof Attributable attributable) {
                double damage = lastRecord.damage();
                AttributeInstance attribute = attributable.getAttribute(Attribute.MAX_HEALTH);
                if (attribute != null) {
                    double maxHealth = attribute.getValue();
                    if (damage >= maxHealth) {
                        Ari.instance.getLog().debug("destine: instant kill, not destine");
                        return false;
                    }
                }
            }
        }

        Set<Entity> attackers = new HashSet<>(resolvedAttackers);
        attackers.remove(null);

        if (attackers.size() > 1) {
            Ari.instance.getLog().debug("destine: attacked by {} unique attackers", attackers.size());
            return true;
        }

        boolean isIndirect = this.isIndirectDamageCause(deathCause);
        if (isIndirect && !attackers.isEmpty()) {
            Ari.instance.getLog().debug("destine: indirect damage with attacker(s)");
            return true;
        }


        if (records.size() >= 2) {
            long firstTime = records.getFirst().timestamp();
            long lastTime = records.getLast().timestamp();
            long duration = lastTime - firstTime;
            if (duration <= 2000 && !attackers.isEmpty()) {
                Ari.instance.getLog().debug("destine: damage duration {} ms within window {} ms", duration, 2000);
                return true;
            }
        }

        // 5. 其余情况返回 false
        Ari.instance.getLog().debug("destine: not destine (direct combat, no attacker, or long time span)");
        return false;
    }

    private boolean evaluateEscape(Entity victim, Entity killer, Location firstAttackLocation, Location deathLocation, EntityDamageEvent.DamageCause cause) {

        if (victim.equals(killer)) return false;

        if (firstAttackLocation == null || deathLocation == null) {
            Ari.instance.getLog().debug("escape check skipped: location missing");
            return false;
        }

        if (!Objects.equals(firstAttackLocation.getWorld(), deathLocation.getWorld())) {
            Ari.instance.getLog().debug("escape check skipped: different worlds");
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

        Ari.instance.getLog().debug("escape evaluated: sqDist={}, threshold={}, escaped={}, cause={}", sqDist, threshold, escaped, cause.name());
        return escaped;
    }

    private CompletableFuture<Entity> resolveAttacker(Entity damager) {
        if (!(damager instanceof Projectile projectile)) return CompletableFuture.completedFuture(damager);

        if (!projectile.isValid()) CompletableFuture.completedFuture(null);
        try {
            Entity shooter = projectile.getShooter() instanceof Entity s ? s : null;
            if (shooter != null) {
                return CompletableFuture.completedFuture(shooter);
            }
            UUID owner = projectile.getOwnerUniqueId();
            if (owner != null) {
                OfflinePlayer ownerEntity = PlayerCache.getPlayer(owner);
                if (ownerEntity instanceof Entity t && t.isValid()) {
                    return CompletableFuture.completedFuture(t);
                }
            }
            return CompletableFuture.completedFuture(damager);
        } catch (Throwable t) {
            Ari.instance.getLog().error(t);
        }

        CompletableFuture<Entity> future = new CompletableFuture<>();
        Consumer<RunTask> task = i -> {
            Entity result = damager;
            try {
                if (projectile.isValid() && !projectile.isDead()) {
                    if (projectile.getShooter() instanceof Entity s) {
                        result = s;
                    } else {
                        UUID owner = projectile.getOwnerUniqueId();
                        if (owner != null) {
                            OfflinePlayer ownerEntity = PlayerCache.getPlayer(owner);
                            if (ownerEntity instanceof Entity t && t.isValid()) {
                                result = t;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            future.complete(result);
        };
        if (projectile.isValid() || !projectile.isDead()) {
            Ari.instance.getScheduler().runAtEntity(projectile, task, null);
        } else {
            Ari.instance.getScheduler().runAtRegion(projectile.getLocation(), task);
        }

        return future;
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