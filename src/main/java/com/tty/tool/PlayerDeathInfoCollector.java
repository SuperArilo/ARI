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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.tty.listener.player.DamageTrackerListener.DAMAGE_TRACKER;

public class PlayerDeathInfoCollector {

    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    public static class DeathInfo {

        public Player victim;
        public long deathTime;
        public EntityDamageEvent.DamageCause deathCause;
        public Entity killer;
        public ItemStack weapon;
        public boolean isEscapeAttempt;
        public boolean isDestine;

        public boolean isProjectile() {
            return deathCause == EntityDamageEvent.DamageCause.PROJECTILE;
        }

        public boolean isDirectCombat() {
            return deathCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                    || deathCause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                    || deathCause == EntityDamageEvent.DamageCause.PROJECTILE;
        }

        @Override
        public String toString() {
            return "DeathInfo{" +
                    "victim=" + victim.getName() +
                    ", deathTime=" + deathTime +
                    ", deathCause=" + deathCause +
                    ", killer=" + (killer != null ? killer.getName() : "null") +
                    ", weapon=" + (weapon != null ? weapon.getType().name() : "null") +
                    ", isEscapeAttempt=" + isEscapeAttempt +
                    ", isDestine=" + isDestine +
                    '}';
        }

        public String getRandomOfList(String keyPath) {
            return getRandomOfList(keyPath, this.isDestine);
        }

        public String getRandomOfList(String keyPath, boolean isDestine) {
            String killerName = killer == null ? null : killer.getType().name().toLowerCase();

            String basePath = isDestine
                    ? keyPath + ".destine"
                    : keyPath;

            List<String> pool = new ArrayList<>();
            List<String> resolvedKeys = new ArrayList<>();

            this.findMessages(basePath, killerName, pool, resolvedKeys);

            return pool.isEmpty()
                    ? ""
                    : pool.get(PublicFunctionUtils.randomGenerator(0, pool.size() - 1));
        }

        private void findMessages(String basePath,
                                  String killerName,
                                  List<String> pool,
                                  List<String> resolvedKeys) {

            if (killerName != null) {
                String killerKey = basePath + "." + killerName;
                List<String> list = Ari.C_INSTANCE.getValue(
                        killerKey,
                        FilePath.DEATH_MESSAGE,
                        STRING_LIST_TYPE,
                        null
                );

                if (list != null && !list.isEmpty()) {
                    pool.addAll(list);
                    resolvedKeys.add(killerKey);
                    return;
                }
            }

            String publicKey = basePath + ".public";
            List<String> list = Ari.C_INSTANCE.getValue(
                    publicKey,
                    FilePath.DEATH_MESSAGE,
                    STRING_LIST_TYPE,
                    null
            );

            if (list != null && !list.isEmpty()) {
                Log.debug("deathMessage hit public key: %s", publicKey);
                pool.addAll(list);
                resolvedKeys.add(publicKey);
            }
        }
    }

    public DeathInfo collect(PlayerDeathEvent event) {
        DeathInfo info = new DeathInfo();
        info.victim = event.getEntity();
        info.deathTime = System.currentTimeMillis();

        EntityDamageEvent last = info.victim.getLastDamageCause();
        info.deathCause = last != null
                ? last.getCause()
                : EntityDamageEvent.DamageCause.CUSTOM;

        Log.debug("death cause resolved: %s", info.deathCause);

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(info.victim);

        Log.debug("damage records count: %s", records.size());

        if (!records.isEmpty()) {
            this.analyzeCombat(records, info);
            if (info.killer != null) {
                Log.debug("combat analysis success, killer: %s", info.killer.getName());
                return info;
            }
        }

        Log.debug("fallback to DamageSource analysis");
        this.fallbackByDamageSource(event, info);
        return info;
    }

    private void analyzeCombat(List<LastDamageTracker.DamageRecord> records, DeathInfo info) {

        Entity firstAttacker = null;
        Entity mainKiller = null;
        ItemStack killWeapon = null;
        Location firstAttackLocation = null;

        for (LastDamageTracker.DamageRecord r : records) {
            Entity actualAttacker = this.resolveAttacker(r.damager());
            if (actualAttacker == null) continue;

            if (firstAttacker == null) {
                firstAttacker = actualAttacker;
                firstAttackLocation = r.location();

                Log.debug("first attacker resolved: %s", actualAttacker.getName());
            }

            mainKiller = actualAttacker;
            killWeapon = r.weapon();
        }

        if (mainKiller == null) {
            Log.debug("no valid attacker found in damage records");
            return;
        }

        info.killer = mainKiller;
        info.weapon = killWeapon;

        Log.debug("main killer resolved: %s, weapon: %s", mainKiller.getName(), killWeapon == null ? "null" : killWeapon.getType().name());

        this.evaluateDestine(info, firstAttacker);
        this.evaluateEscape(info, firstAttackLocation);
    }

    private void evaluateDestine(DeathInfo info, Entity firstAttacker) {
        if (!info.killer.equals(firstAttacker)) {
            info.isDestine = true;
        } else {
            info.isDestine = !info.isDirectCombat();
        }

        Log.debug("destine evaluated: %s (firstAttacker=%s, directCombat=%s)", info.isDestine, firstAttacker == null ? "null" : firstAttacker.getName(), info.isDirectCombat());
    }

    private void evaluateEscape(DeathInfo info, Location firstAttackLocation) {
        if (firstAttackLocation == null) return;
        if (firstAttackLocation.getWorld() != info.victim.getWorld()) return;

        double escapeDistance = info.isProjectile() ? 20.0 : 1.5;
        double sqDist = firstAttackLocation.distanceSquared(
                info.victim.getLocation());

        info.isEscapeAttempt = sqDist > escapeDistance * escapeDistance;

        Log.debug("escape evaluated: %s (sqDist=%s, threshold=%s)", info.isEscapeAttempt, sqDist, escapeDistance * escapeDistance);
    }

    private void fallbackByDamageSource(PlayerDeathEvent event, DeathInfo info) {
        DamageSource source = event.getDamageSource();
        info.killer = source.getCausingEntity();

        Log.debug("fallback killer resolved: %s", info.killer == null ? "null" : info.killer.getName());

        if (info.killer instanceof LivingEntity e) {
            EntityEquipment eq = e.getEquipment();
            info.weapon = eq == null ? null : eq.getItemInMainHand();

            Log.debug("fallback weapon resolved: %s", info.weapon == null ? "null" : info.weapon.getType().name());
        }
    }

    private Entity resolveAttacker(Entity damager) {
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter) {
                return shooter;
            }

            UUID ownerId = projectile.getOwnerUniqueId();
            if (ownerId != null) {
                return Bukkit.getPlayer(ownerId);
            }

            return damager;
        }
        return damager;
    }
}
