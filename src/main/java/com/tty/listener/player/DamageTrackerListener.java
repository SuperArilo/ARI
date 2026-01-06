package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.task.CancellableTask;
import com.tty.tool.LastDamageTracker;
import org.bukkit.attribute.Attributable;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DamageTrackerListener implements Listener {

    public static final LastDamageTracker DAMAGE_TRACKER = new LastDamageTracker();

    //用于清理超过20秒后的被攻击的实体记录
    private static final long CLEAR_LAST_ATTACK_RECORD = 20_000L;
    //用于定时清理受害者的记录周期
    private static final long TICK_CLEAR_DEALY = 30 * 20L;

    private CancellableTask cleanTask;

    public DamageTrackerListener() {
        this.cleanTask = this.createCleanTask();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable)) return;

        Entity rawDamager = event.getDamager();
        Entity attacker = rawDamager;

        if (rawDamager instanceof Projectile projectile
                && projectile.getShooter() instanceof Entity shooter) {
            attacker = shooter;
        }

        if (!(attacker instanceof Player) && !(victim instanceof Player)) return;

        int hash = Objects.hash(event);
        if (this.hasSameRecord(victim, hash)) return;

        DAMAGE_TRACKER.addRecord(
                hash,
                victim,
                attacker,
                event.getFinalDamage(),
                this.getWeapon(attacker, victim)
        );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable && victim instanceof Attributable)) return;

        DamageSource damageSource = event.getDamageSource();
        Entity attacker = damageSource.getCausingEntity();
        Entity directEntity = damageSource.getDirectEntity();

        int hash = Objects.hash(event);
        if (this.hasSameRecord(victim, hash)) return;

        if (event.getCause().equals(EntityDamageEvent.DamageCause.MAGIC)) {
            LastDamageTracker.DamageRecord last = DAMAGE_TRACKER.getRecords(victim).getLast();
            if (last != null) {
                attacker = last.damager();
            }
        }

        if (attacker == null) return;

        DAMAGE_TRACKER.addRecord(
                hash,
                victim,
                attacker,
                event.getFinalDamage(),
                this.getWeapon(attacker, directEntity)
        );
    }

    private @Nullable ItemStack getWeapon(Entity attacker, Entity directEntity) {
        ItemStack weapon = null;
        if (directEntity != null) {
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
                default -> {}
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

    private boolean hasSameRecord(Entity victim, int hash) {
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records.isEmpty()) return false;
        LastDamageTracker.DamageRecord last = records.getLast();
        return last.hash() == hash;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        DAMAGE_TRACKER.clearRecords(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DAMAGE_TRACKER.clearRecords(entity);
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        DAMAGE_TRACKER.removeAll();
        if (this.cleanTask != null) {
            this.cleanTask.cancel();
            this.cleanTask = null;
        }
        this.cleanTask = this.createCleanTask();
    }

    private CancellableTask createCleanTask() {
        if (this.cleanTask != null) {
            this.cleanTask.cancel();
            this.cleanTask = null;
        }
        return Lib.Scheduler.runAtFixedRate(Ari.instance, i -> {
            long now = System.currentTimeMillis();
            Set<Entity> victims = DAMAGE_TRACKER.getVictimsSnapshot();
            for (Entity e : victims) {
                if (!(e instanceof Damageable damageable)) continue;
                long lastTs = DAMAGE_TRACKER.getLastTimestamp(damageable);
                if (lastTs == 0L || (now - lastTs) > CLEAR_LAST_ATTACK_RECORD) {
                    DAMAGE_TRACKER.clearRecords(damageable);
                    Lib.Scheduler.runAtEntity(
                            Ari.instance,
                            e,
                            t -> Log.debug("damage_tracker: remove victim entity %s record.", e.getName()),
                            null
                    );
                }
            }
        }, 1L, TICK_CLEAR_DEALY);
    }

}
