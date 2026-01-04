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

        ItemStack weapon = null;
        if (attacker instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) weapon = eq.getItemInMainHand();
        }

        //因为 EntityDamageEvent 比 EntityDamageByEntityEvent 先执行，所以先排除相同伤害
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (!records.isEmpty()) {
            LastDamageTracker.DamageRecord last = records.getLast();
            if (last.hash() == Objects.hash(event)) return;
        }

        DAMAGE_TRACKER.addRecord(
                Objects.hash(event),
                victim,
                attacker,
                event.getFinalDamage(),
                weapon
        );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable && victim instanceof Attributable)) return;

        long now = System.currentTimeMillis();

        Entity attacker = event.getDamageSource().getCausingEntity();
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);

        int hash = Objects.hash(event);

        // 攻击者回溯
        if (!records.isEmpty()) {
            for (int i = records.size() - 1; i >= 0; i--) {
                LastDamageTracker.DamageRecord r = records.get(i);
                attacker = r.damager();
            }
        }

        if (attacker == null) return;

        ItemStack weapon = null;
        Entity directEntity = event.getDamageSource().getDirectEntity();
        if (directEntity != null) {
            if (directEntity instanceof LivingEntity living) {
                EntityEquipment eq = living.getEquipment();
                if (eq != null) {
                    weapon = eq.getItemInMainHand();
                }
            } else if (directEntity instanceof Item dropped) {
                weapon = dropped.getItemStack();
            }
        }

        DAMAGE_TRACKER.addRecord(
                hash,
                victim,
                attacker,
                event.getFinalDamage(),
                weapon
        );
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
