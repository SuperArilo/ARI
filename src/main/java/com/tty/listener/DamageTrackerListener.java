package com.tty.listener;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.api.task.CancellableTask;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.enumType.FilePath;
import com.tty.tool.LastDamageTracker;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class DamageTrackerListener implements Listener {

    public static final LastDamageTracker DAMAGE_TRACKER = new LastDamageTracker();

    //用于清理超过20秒后的被攻击的实体记录
    private int clear_last_attack_record;
    //用于定时清理受害者的记录周期
    private int tick_clear_dealy;

    private List<EntityType> excludedEntities = new ArrayList<>();

    private CancellableTask cleanTask;

    public DamageTrackerListener() {
        this.excludedEntities = this.loadExcludedEntities();
        this.clear_last_attack_record = this.loadClearLastAttackRecord();
        this.tick_clear_dealy = this.loadTickClearDealy();
        this.cleanTask = this.createCleanTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntity(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable victim)) return;
        if (this.excludedEntities.contains(entity.getType())) return;

        DamageSource damageSource = event.getDamageSource();
        Entity causingEntity = damageSource.getCausingEntity();
        Entity directEntity = damageSource.getDirectEntity();

        LastDamageTracker.AttackCheckResult result = DAMAGE_TRACKER.checkAttackInvolvesPlayer(victim, causingEntity, directEntity, event.getCause());
        if (!result.involvesPlayer()) return;
        Entity attacker = result.resolvedAttacker();
        DAMAGE_TRACKER.addRecord(
                victim,
                attacker,
                event.getFinalDamage(),
                DAMAGE_TRACKER.getWeapon(attacker, directEntity, causingEntity)
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        DAMAGE_TRACKER.clearRecords(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
        this.tick_clear_dealy = this.loadTickClearDealy();
        this.clear_last_attack_record = this.loadClearLastAttackRecord();
        this.excludedEntities = this.loadExcludedEntities();
        this.cleanTask = this.createCleanTask();
    }

    private CancellableTask createCleanTask() {
        if (this.cleanTask != null) {
            this.cleanTask.cancel();
            this.cleanTask = null;
        }
        return Ari.instance.getScheduler().runAtFixedRate(Ari.instance, i -> {
            long now = System.currentTimeMillis();
            Set<Entity> victims = DAMAGE_TRACKER.getVictimsSnapshot();
            for (Entity e : victims) {
                if (!(e instanceof Damageable damageable)) continue;
                long lastTs = DAMAGE_TRACKER.getLastTimestamp(damageable);
                if (lastTs == 0L || (now - lastTs) > this.clear_last_attack_record * 1000L) {
                    DAMAGE_TRACKER.clearRecords(damageable);
                    Ari.instance.getScheduler().runAtEntity(
                            Ari.instance,
                            e,
                            t -> Ari.instance.getLog().debug("damage_tracker: remove victim entity {} record.", e.getName()),
                            null
                    );
                }
            }
        }, 1L, this.tick_clear_dealy * 20L);
    }

    private List<EntityType> loadExcludedEntities() {
        List<String> value = Ari.instance.getConfigInstance().getValue(
                "attack-bar.damage-tracker.excluded-entities",
                FilePath.ATTACK_BAR_CONFIG,
                new TypeToken<List<String>>() {}.getType(),
                List.of());
        return PublicFunctionUtils.convertStringListToEnumList(value, EntityType.class, false);
    }

    private int loadTickClearDealy() {
        return Ari.instance.getConfigInstance().getValue("attack-bar.damage-tracker.tick_clear_dealy", FilePath.ATTACK_BAR_CONFIG, Integer.class, 30);
    }

    private int loadClearLastAttackRecord() {
        return Ari.instance.getConfigInstance().getValue("attack-bar.damage-tracker.clear_last_attack_record", FilePath.ATTACK_BAR_CONFIG, Integer.class, 30);
    }

}
