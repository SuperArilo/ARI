package com.tty.tool;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Type;
import java.util.*;

public class PlayerDeathInfoCollector {

    public static class DeathInfo {

        public Player victim;
        public long deathTime;
        public EntityDamageEvent.DamageCause deathCause;
        public EntityDamageEvent event;
        public boolean isEntityCause;
        public boolean isProjectile;
        public Entity killer;
        public ItemStack weapon;
        public boolean isEscapeAttempt;

        @Override
        public String toString() {
            return "DeathInfo{" +
                    "victim=" + victim.getName() +
                    ", deathTime=" + deathTime +
                    ", deathCause=" + deathCause +
                    ", event=" + event +
                    ", isEntityCause=" + isEntityCause +
                    ", isProjectile=" + isProjectile +
                    ", killer=" + (killer != null ? killer.getName() : "null") +
                    ", weapon=" + (weapon != null ? weapon.getType().name() : "null") +
                    ", isEscapeAttempt=" + isEscapeAttempt +
                    '}';
        }

        public String getRandomOfList(String keyPath) {
            String killerName = this.killer == null ? null : this.killer.getType().name().toLowerCase();
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> pool = new ArrayList<>();

            List<String> publicList = Ari.C_INSTANCE.getValue(keyPath + ".public", FilePath.DEATH_MESSAGE, type, null);

            if (publicList != null && !publicList.isEmpty()) {
                pool.addAll(publicList);
                if (killerName != null) {
                    List<String> killerList = Ari.C_INSTANCE.getValue(keyPath + "." + killerName, FilePath.DEATH_MESSAGE, type, null);
                    if (killerList != null && !killerList.isEmpty()) pool.addAll(killerList);
                }
            } else {
                if (killerName != null) {
                    List<String> killerList = Ari.C_INSTANCE.getValue(keyPath + "." + killerName, FilePath.DEATH_MESSAGE, type,null);
                    if (killerList != null && !killerList.isEmpty()) pool.addAll(killerList);
                    else {
                        List<String> fallbackList = Ari.C_INSTANCE.getValue(keyPath, FilePath.DEATH_MESSAGE, type, null);
                        if (fallbackList != null && !fallbackList.isEmpty()) pool.addAll(fallbackList);
                    }
                }
            }
            return pool.isEmpty() ? "" :
                    pool.get(PublicFunctionUtils.randomGenerator(0, pool.size() - 1));
        }

    }

    public DeathInfo collect(PlayerDeathEvent event, LastDamageTracker tracker) {
        DeathInfo info = new DeathInfo();
        info.victim = event.getEntity();
        info.deathTime = System.currentTimeMillis();
        info.event = info.victim.getLastDamageCause();
        info.deathCause = info.event != null ? info.event.getCause() : EntityDamageEvent.DamageCause.CUSTOM;
        info.isEntityCause = false;
        info.isProjectile = false;
        info.killer = null;
        info.weapon = null;
        info.isEscapeAttempt = false;

        List<LastDamageTracker.DamageRecord> records = tracker.getRecords(info.victim.getUniqueId());

        if (!records.isEmpty()) {
            Map<Entity, Double> damageSum = new HashMap<>();
            for (LastDamageTracker.DamageRecord r : records) {
                Entity realDamager = getActualDamager(r.damager());
                if (realDamager != null && realDamager.isValid()) {
                    damageSum.put(realDamager, damageSum.getOrDefault(realDamager, 0.0) + r.damage());
                }
            }

            Entity mainKiller = damageSum.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (mainKiller != null) {
                info.killer = mainKiller;
                info.isEntityCause = true;

                LastDamageTracker.DamageRecord lastRecord = records.stream()
                        .filter(r -> getActualDamager(r.damager()) == mainKiller)
                        .reduce((first, second) -> second)
                        .orElse(null);

                if (lastRecord != null) {
                    if (!(info.deathCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                            || info.deathCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                        info.weapon = lastRecord.weapon();
                    }
                    info.isProjectile = lastRecord.isProjectile() || lastRecord.damager() instanceof Projectile;
                    double escapeDistance = info.isProjectile ? 1.5 : 20.0;
                    Location lastLoc = lastRecord.location();
                    Location deathLoc = info.victim.getLocation();
                    if (lastLoc != null && lastLoc.getWorld() == deathLoc.getWorld()) {
                        double distSq = lastLoc.distanceSquared(deathLoc);
                        info.isEscapeAttempt = distSq > escapeDistance * escapeDistance;
                    }
                }
            }
        }

        if (info.deathCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (info.event instanceof EntityDamageByEntityEvent damageEvent) {
                Entity damager = damageEvent.getDamager();
                if (damager instanceof Creeper ||
                        damager instanceof TNTPrimed ||
                        damager instanceof Wither ||
                        damager instanceof EnderCrystal) {
                    info.killer = damager;
                    info.isEntityCause = true;
                    info.weapon = null;
                    info.isProjectile = false;
                }
            }
        }

        tracker.clearRecords(info.victim.getUniqueId());
        return info;
    }

    private Entity getActualDamager(Entity damager) {
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Entity entityShooter) return entityShooter;
        }
        return damager;
    }

}
