package com.tty.tool;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.lib.Log;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.*;

import static com.tty.listener.player.DamageTrackerListener.DAMAGE_TRACKER;

public class PlayerDeathInfoCollector {

    public static class DeathInfo {

        public Player victim;
        public long deathTime;
        public EntityDamageEvent.DamageCause deathCause;
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
            List<String> resolvedKeys = new ArrayList<>();
            List<String> publicList = Ari.C_INSTANCE.getValue(keyPath + ".public", FilePath.DEATH_MESSAGE, type, null);

            if (publicList != null && !publicList.isEmpty()) {
                pool.addAll(publicList);
                resolvedKeys.add(keyPath + ".public");

                if (killerName != null) {
                    List<String> killerList = Ari.C_INSTANCE.getValue(keyPath + "." + killerName, FilePath.DEATH_MESSAGE, type, null);
                    if (killerList != null && !killerList.isEmpty()) {
                        pool.addAll(killerList);
                        resolvedKeys.add(keyPath + "." + killerName);
                    }
                }
            } else {
                if (killerName != null) {
                    List<String> killerList = Ari.C_INSTANCE.getValue(keyPath + "." + killerName, FilePath.DEATH_MESSAGE, type, null);
                    if (killerList != null && !killerList.isEmpty()) {
                        pool.addAll(killerList);
                        resolvedKeys.add(keyPath + "." + killerName);
                    } else {
                        List<String> fallbackList = Ari.C_INSTANCE.getValue(keyPath, FilePath.DEATH_MESSAGE, type, null);
                        if (fallbackList != null && !fallbackList.isEmpty()) {
                            pool.addAll(fallbackList);
                            resolvedKeys.add(keyPath);
                        }
                    }
                }
            }
            Log.debug("full key path: %s", String.join(", ", resolvedKeys));
            return pool.isEmpty() ? "" :
                    pool.get(PublicFunctionUtils.randomGenerator(0, pool.size() - 1));
        }
    }

    public DeathInfo collect(PlayerDeathEvent event) {
        DeathInfo info = new DeathInfo();
        info.victim = event.getEntity();
        info.deathTime = System.currentTimeMillis();
        EntityDamageEvent cause = info.victim.getLastDamageCause();
        info.deathCause = cause != null ? cause.getCause() : EntityDamageEvent.DamageCause.CUSTOM;
        info.isEntityCause = false;
        info.isProjectile = info.deathCause.equals(EntityDamageEvent.DamageCause.PROJECTILE);
        info.killer = null;
        info.weapon = null;
        info.isEscapeAttempt = false;

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(info.victim);

        if (!records.isEmpty()) {
            LastDamageTracker.DamageRecord last = records.getLast();
            Entity mainKiller = last.damager();
            if (mainKiller != null) {
                info.killer = mainKiller;
                info.isEntityCause = true;
                double escapeDistance = !info.isProjectile ? 1.5 : 20.0;
                Location lastLoc = records.getFirst().location();
                Location deathLoc = info.victim.getLocation();
                if (lastLoc != null && lastLoc.getWorld() == deathLoc.getWorld()) {
                    double sqDist = lastLoc.distanceSquared(deathLoc);
                    Log.debug("Distance squared: %s, Threshold: %s", sqDist, (escapeDistance * escapeDistance));
                    info.isEscapeAttempt = lastLoc.distanceSquared(deathLoc) > escapeDistance * escapeDistance;
                }

                info.weapon = last.weapon();
            }
            return info;
        }
        DamageSource damageSource = event.getDamageSource();

        info.killer = damageSource.getCausingEntity();
        if (info.killer instanceof LivingEntity e) {
            EntityEquipment equipment = e.getEquipment();
            info.weapon = equipment == null ? null:equipment.getItemInMainHand();
            return info;
        }

        if (info.deathCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (cause instanceof EntityDamageByEntityEvent damageEvent) {
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

        return info;
    }

}
