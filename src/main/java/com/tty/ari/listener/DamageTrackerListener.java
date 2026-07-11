package com.tty.ari.listener;

import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.api.task.CancellableTask;
import com.tty.ari.Ari;
import com.tty.ari.configuration.AttackBarConfig;
import com.tty.ari.tool.LastDamageTracker;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;

public class DamageTrackerListener implements Listener {

    public static final LastDamageTracker DAMAGE_TRACKER = new LastDamageTracker();

    private CancellableTask cleanTask;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntity(EntityDamageEvent event) {
        if (Ari.instance.getConfigurationManager().get(AttackBarConfig.class).getExcludedEntities().stream().anyMatch(i -> i.equalsIgnoreCase(event.getEntity().getType().name()))) return;
        DAMAGE_TRACKER.addRecord(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DAMAGE_TRACKER.clearRecords(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        DAMAGE_TRACKER.clearRecords(event.getEntity());
    }

    @EventHandler
    public void onPluginReload(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
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
        AttackBarConfig attackBarConfig = Ari.instance.getConfigurationManager().get(AttackBarConfig.class);
        return Ari.instance.getScheduler().runAtFixedRate(i -> {
            long now = System.currentTimeMillis();
            Set<Entity> victims = DAMAGE_TRACKER.getVictimsSnapshot();
            for (Entity e : victims) {
                if (!(e instanceof Damageable damageable)) continue;
                long lastTs = DAMAGE_TRACKER.getLastTimestamp(damageable);
                if (lastTs == 0L || (now - lastTs) > attackBarConfig.getClearLastAttackRecord() * 1000L) {
                    DAMAGE_TRACKER.clearRecords(damageable);
                    Ari.instance.getScheduler().runAtEntity(e, t -> Ari.instance.getLog().debug("damage_tracker: remove victim entity {} record.", e.getName()), null);
                }
            }
        }, 1L, attackBarConfig.getTickClearDealy() * 20L);
    }
}
