package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.task.CancellableTask;
import com.tty.tool.LastDamageTracker;
import org.bukkit.attribute.Attributable;
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

import java.util.*;

public class DamageTrackerListener implements Listener {

    public static final LastDamageTracker DAMAGE_TRACKER = new LastDamageTracker();
    private static final long DOT_ATTacker_TTL_MS = 5_000L;
    private CancellableTask cleanTask;

    public DamageTrackerListener() {
        this.cleanTask = this.createCleanTask();
    }

    @EventHandler(priority = EventPriority.LOW)
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

        ItemStack weapon = null;
        if (attacker instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) weapon = eq.getItemInMainHand();
        }

        DAMAGE_TRACKER.addRecord(
                victim,
                attacker,
                event.getFinalDamage(),
                weapon
        );
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable && victim instanceof Attributable)) return;

        //排除近战主动攻击
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        long now = System.currentTimeMillis();

        Entity attacker = event.getDamageSource().getCausingEntity();

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (!records.isEmpty()) {
            for (int i = records.size() - 1; i >= 0; i--) {
                LastDamageTracker.DamageRecord r = records.get(i);
                boolean a = now - r.timestamp() > DOT_ATTacker_TTL_MS;
                if (a) {
                    records.remove(i);
                    continue;
                }

                attacker = r.damager();
            }
        }
        if (attacker == null) return;

        //判断是否需要添加记录
        boolean shouldAddRecord = false;

        long lastPlayerTs = 0L;
        for (int i = records.size() - 1; i >= 0; i--) {
            LastDamageTracker.DamageRecord r = records.get(i);
            if (r.damager().equals(attacker)) {
                lastPlayerTs = r.timestamp();
                break;
            }
        }
        if (lastPlayerTs == 0L || now - lastPlayerTs > 50L) {
            shouldAddRecord = true;
        }

        //获取武器信息
        ItemStack weapon = null;
        Entity directEntity = event.getDamageSource().getDirectEntity();
        if (directEntity != null) {
            if (directEntity instanceof LivingEntity living) {
                EntityEquipment eq = living.getEquipment();
                if (eq != null) weapon = eq.getItemInMainHand();
            } else if (directEntity instanceof Item dropped) {
                weapon = dropped.getItemStack();
            }
        }

        if (shouldAddRecord) {
            DAMAGE_TRACKER.addRecord(victim, attacker, event.getFinalDamage(), weapon);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        DAMAGE_TRACKER.clearRecords(event.getPlayer());
    }

    @EventHandler
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

                if (lastTs == 0L || (now - lastTs) > 20_000L) {
                    DAMAGE_TRACKER.clearRecords(damageable);
                    Lib.Scheduler.runAtEntity(Ari.instance, e, t -> Log.debug("remove tracker entity %s record.", e.getName()), null);
                }
            }
        }, 1L, 30 * 20L);
    }

}
