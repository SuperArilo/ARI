package com.tty.tool;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.lib.Log;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
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
            Log.debug("DeathMessage resolved keys: %s", String.join(", ", resolvedKeys));
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

        Log.debug("death cause resolved: %s", info.deathCause);

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(info.victim);
        Log.debug("damage records count: %d", records.size());

        if (!records.isEmpty()) {
            Entity firstAttacker = null;
            Entity mainKiller = null;
            ItemStack killWeapon = null;
            Location firstAttackLocation = null;

            for (LastDamageTracker.DamageRecord r : records) {
                Entity damager = r.damager();
                if (damager == null) continue;
                Entity actual = resolveAttacker(damager);
                if (actual == null) continue;
                if (firstAttacker == null) {
                    firstAttacker = actual;
                    firstAttackLocation = r.location();
                    Log.debug("first attacker resolved: %s", actual.getType().name());
                }
                mainKiller = actual;
                killWeapon = r.weapon();
            }

            if (mainKiller != null) {
                info.killer = mainKiller;
                info.weapon = killWeapon;
                boolean directCombat = info.deathCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || info.deathCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK || info.deathCause == EntityDamageEvent.DamageCause.PROJECTILE;
                info.isDestine = !mainKiller.equals(firstAttacker) || !directCombat;
                Log.debug("destine evaluated: %s (firstAttacker=%s, directCombat=%s)", info.isDestine, firstAttacker.getType().name(), directCombat);
                info.isEscapeAttempt = evaluateEscape(firstAttackLocation, info.victim.getLocation(), info.deathCause);
                Log.debug("combat analysis success, killer: %s", mainKiller.getType().name());
                return info;
            }
        }

        DamageSource source = event.getDamageSource();
        info.killer = source.getCausingEntity();

        if (info.killer instanceof LivingEntity e) {
            EntityEquipment eq = e.getEquipment();
            info.weapon = eq == null ? null : eq.getItemInMainHand();
        }

        Log.debug("fallback analysis used, killer: %s", info.killer != null ? info.killer.getType().name() : "null");
        return info;
    }

    private boolean evaluateEscape(Location firstAttackLocation, Location deathLocation, EntityDamageEvent.DamageCause cause) {
        if (firstAttackLocation == null || deathLocation == null) {
            Log.debug("escape check skipped: location missing");
            return false;
        }
        if (!Objects.equals(firstAttackLocation.getWorld(), deathLocation.getWorld())) {
            Log.debug("escape check skipped: different worlds");
            return false;
        }
        boolean ranged = cause == EntityDamageEvent.DamageCause.PROJECTILE;
        double escapeDistance = ranged ? 20.0 : 1.5;
        double sqDist = firstAttackLocation.distanceSquared(deathLocation);
        double threshold = escapeDistance * escapeDistance;
        boolean escaped = sqDist > threshold;
        Log.debug("escape evaluated: sqDist=%.2f, threshold=%.2f, escaped=%s", sqDist, threshold, escaped);
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
}
