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

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Player attacker;
        Entity damager = event.getDamager();
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player shooter) attacker = shooter;
            else attacker = null;
        } else {
            attacker = null;
        }
        if (attacker == null) return;

        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable victimDamageable)) return;

        ItemStack weapon = null;
        if (damager instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) weapon = eq.getItemInMainHand();
        }

        DAMAGE_TRACKER.addRecord(
                victimDamageable,
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
        Player attackerPlayer = null;

        //直接攻击来源
        Entity causing = event.getDamageSource().getCausingEntity();
        if (causing instanceof Player p) {
            attackerPlayer = p;
        }

        //查找最近玩家造成的伤害
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (!records.isEmpty()) {
            for (int i = records.size() - 1; i >= 0; i--) {
                LastDamageTracker.DamageRecord r = records.get(i);

                //超时则移除
                if (now - r.timestamp() > DOT_ATTacker_TTL_MS) {
                    records.remove(i);
                    continue;
                }

                Entity damager = r.damager();
                if (damager instanceof Player p) {
                    attackerPlayer = attackerPlayer == null ? p : attackerPlayer;
                    break;
                }
            }
        }

        if (attackerPlayer == null || !attackerPlayer.isOnline()) return;

        //判断是否需要添加记录
        boolean shouldAddRecord = false;
        if (causing instanceof Player) {
            long lastPlayerTs = 0L;
            for (int i = records.size() - 1; i >= 0; i--) {
                LastDamageTracker.DamageRecord r = records.get(i);
                if (r.damager() instanceof Player p && p.equals(attackerPlayer)) {
                    lastPlayerTs = r.timestamp();
                    break;
                }
            }
            if (lastPlayerTs == 0L || now - lastPlayerTs > 50L) {
                shouldAddRecord = true;
            }
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
            DAMAGE_TRACKER.addRecord(victim, attackerPlayer, event.getFinalDamage(), weapon);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        this.removePlayerRecord(event.getPlayer());
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

    private void removePlayerRecord(Player player) {
        DAMAGE_TRACKER.clearDamagerRecords(player);
    }

    private CancellableTask createCleanTask() {
        if (this.cleanTask != null) {
            this.cleanTask.cancel();
            this.cleanTask = null;
        }
        return Lib.Scheduler.runAtFixedRate(Ari.instance, i -> {
            long now = System.currentTimeMillis();
            Set<Entity> victims = DAMAGE_TRACKER.getVictimsSnapshot();
            int t = 0;
            for (Entity e : victims) {
                if (!(e instanceof Damageable mob)) continue;
                long lastTs = DAMAGE_TRACKER.getLastTimestamp(mob);

                if (lastTs == 0L || (now - lastTs) > 20_000L) {
                    t++;
                    DAMAGE_TRACKER.clearRecords(mob);
                }
            }
            if (t == 0) return;
            Log.debug("remove tracker record %s.", t);
        }, 1L, 30 * 20L);
    }

}
