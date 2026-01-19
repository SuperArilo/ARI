package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.bar.PlayerAttackBar;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.task.CancellableTask;
import com.tty.lib.tool.FormatUtils;
import com.tty.tool.ConfigUtils;
import com.tty.tool.LastDamageTracker;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.tty.listener.DamageTrackerListener.DAMAGE_TRACKER;

public class MobBossBarListener implements Listener {

    private long clear_last_attack_record;
    private long tick_clear_dealy;
    private int maxBar = 0;
    private boolean isDisabled = true;
    private final Map<Player, LinkedHashMap<Damageable, PlayerAttackBar>> playerBars = new ConcurrentHashMap<>();

    private CancellableTask cleanTask;

    public MobBossBarListener() {
        this.tick_clear_dealy = this.loadTick_clear_dealy();
        this.clear_last_attack_record = this.loadClear_last_attack_record();
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
        this.cleanTask = this.createCleanTask();
    }

    private void debugLog(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Damageable victim)) return;
        double health = Math.max(0, victim.getHealth() - event.getFinalDamage());
        Entity attacker = null;
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records != null) {
            attacker = records.getLast().damager();
        }
        if (attacker == null) {
            attacker = event.getDamageSource().getDirectEntity();
        }
        Log.debug("attacker: {}, event: {}, entity: {}, damage: {}, health: {}, damageType: {}, status: {}.",
                attacker == null ? "null":attacker.getName(),
                event.getEventName(),
                victim.getName(),
                FormatUtils.formatTwoDecimalPlaces(event.getFinalDamage()),
                FormatUtils.formatTwoDecimalPlaces(health),
                event.getCause().name(),
                health == 0F ? "death":"living")
        ;
    }

    private boolean isBoss(Damageable damageable) {
        return (damageable instanceof Boss);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.isDisabled) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable victim)) return;

        // 如果是boss，不显示bar
        if (this.isBoss(victim)) return;

        // 获取伤害记录
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records.isEmpty()) return;

        // 获取最近一次伤害记录
        LastDamageTracker.DamageRecord last = records.getLast();

        // 攻击者必须是玩家
        if (!(last.damager() instanceof Player player)) return;

        // 如果是自己造成的伤害不显示
        if (victim.equals(player)) return;

        // 更新BossBar
        this.updateBar(event.getFinalDamage(), victim, player);
        this.debugLog(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void regainHealth(EntityRegainHealthEvent event) {
        if (this.isDisabled) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable victim) || entity instanceof Player) return;
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records.isEmpty()) return;

        LastDamageTracker.DamageRecord last = records.getLast();
        if (!(last.damager() instanceof Player player)) return;

        this.updateBar(-event.getAmount(), victim, player);
    }

    /**
     * 更新玩家 bar
     * @param finalDamage 收到的伤害，如果是负数则是治疗
     * @param damageable 被玩家攻击的对象
     * @param player 玩家
     */
    private void updateBar(double finalDamage, Damageable damageable, Player player) {
        if (!(damageable instanceof Attributable attr)) return;

        LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.get(player);
        if (bars == null) {
            LinkedHashMap<Damageable, PlayerAttackBar> created = new LinkedHashMap<>();
            LinkedHashMap<Damageable, PlayerAttackBar> race = this.playerBars.putIfAbsent(player, created);
            bars = (race == null) ? created : race;
        }

        while (bars.size() >= this.maxBar) {
            Map.Entry<Damageable, PlayerAttackBar> oldest = bars.entrySet().iterator().next();
            oldest.getValue().remove(player);
            bars.remove(oldest.getKey());
        }

        PlayerAttackBar bar = bars.get(damageable);

        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute == null ? 1 : attribute.getValue();
        double currentHealth = Math.max(0, Math.min(maxHealth, damageable.getHealth() - finalDamage));
        double healthRatio = (currentHealth / Math.max(1, maxHealth));

        Component t = ConfigUtils.tAfter(
                "server.boss-bar.player-attack",
                Map.of(
                        LangType.MOB.getType(), damageable.name(),
                        LangType.MOB_CURRENT_HEALTH.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(currentHealth)).color(this.getMobHealthTextColor(healthRatio)),
                        LangType.MOB_MAX_HEALTH.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))
                )
        );

        if (bar == null || bar.isRemoved()) {
            if (bar != null) {
                bar.remove(player);
            }
            bar = new PlayerAttackBar(player, t, Float.parseFloat(FormatUtils.formatTwoDecimalPlaces(healthRatio)), this.getMobBarColor(healthRatio));
            bars.put(damageable, bar);
        } else {
            bar.setName(t);
        }
        if (bar.getProgress() != healthRatio) {
            bar.setProgress(Float.parseFloat(FormatUtils.formatTwoDecimalPlaces(Math.max(0, healthRatio))));
            bar.setColor(this.getMobBarColor(healthRatio));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        if (this.isDisabled) return;
        this.removePlayerRecord(event.getPlayer());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (this.isDisabled) return;
        Damageable deadEntity = event.getEntity();
        if (deadEntity instanceof Player player) {
            this.removePlayerRecord(player);
        } else {
            var affectedPlayers = new LinkedHashMap<Player, PlayerAttackBar>();
            this.playerBars.forEach((player, bars) -> {
                PlayerAttackBar bar = bars.get(deadEntity);
                if (bar != null) {
                    affectedPlayers.put(player, bar);
                }
            });
            if (affectedPlayers.isEmpty()) return;

            Lib.Scheduler.runAtEntity(Ari.instance, deadEntity, i ->
                    affectedPlayers.forEach((player, bar) -> {
                        LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.get(player);
                        if (bars != null) bars.remove(deadEntity);
                    }), null);
        }
    }

    private void removePlayerRecord(Player player) {
        LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.remove(player);
        if (bars != null) {
            bars.values().forEach(bar -> bar.remove(player));
        }
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
        this.tick_clear_dealy = this.loadTick_clear_dealy();
        this.clear_last_attack_record = this.loadClear_last_attack_record();
        if (this.isDisabled) {
            this.playerBars.forEach((player, bars) -> bars.values().forEach(bar -> bar.remove(player)));
            this.playerBars.clear();
            if (this.cleanTask != null) {
                this.cleanTask.cancel();
                this.cleanTask = null;
            }
            this.cleanTask = this.createCleanTask();
        }
    }

    private BossBar.Color getMobBarColor(double ratio) {
        if (ratio >= 0.7f) return BossBar.Color.GREEN;
        if (ratio >= 0.3f) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }

    private TextColor getMobHealthTextColor(double ratio) {
        if (ratio >= 0.7f) return TextColor.color(0x55FF55);
        if (ratio >= 0.3f) return TextColor.color(0xFFFF55);
        return TextColor.color(0xFF5555);
    }

    private boolean isDisabled() {
        return !Ari.C_INSTANCE.getValue("attack-boss-bar.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

    private int getMaxBar() {
        return Ari.C_INSTANCE.getValue("attack-boss-bar.max-bar", FilePath.FUNCTION_CONFIG, Integer.class, 1);
    }

    private CancellableTask createCleanTask() {
        if (this.cleanTask != null) {
            this.cleanTask.cancel();
            this.cleanTask = null;
        }
        return Lib.Scheduler.runAtFixedRate(Ari.instance, i -> {
            long now = System.currentTimeMillis();
            Map<Player, Integer> removedCountByPlayer = new LinkedHashMap<>();
            for (Map.Entry<Player, LinkedHashMap<Damageable, PlayerAttackBar>> entry : this.playerBars.entrySet()) {
                Player player = entry.getKey();
                LinkedHashMap<Damageable, PlayerAttackBar> bars = entry.getValue();
                Iterator<Map.Entry<Damageable, PlayerAttackBar>> it = bars.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Damageable, PlayerAttackBar> barEntry = it.next();
                    Damageable mob = barEntry.getKey();
                    PlayerAttackBar bar = barEntry.getValue();
                    long lastAttackTs = DAMAGE_TRACKER.getLastTimestamp(mob);
                    if (bar.isRemoved() || lastAttackTs == 0 || (now - lastAttackTs) > this.clear_last_attack_record * 1000L) {
                        bar.remove(player);
                        it.remove();
                        removedCountByPlayer.merge(player, 1, Integer::sum);
                    }
                }
                if (bars.isEmpty()) {
                    this.playerBars.remove(player);
                }
            }
            removedCountByPlayer.forEach((player, count) ->
                    Log.debug("mob bar expired: player={}, removedEntities={}, current_bar_count={}, max_bar_count={}",
                            player.getName(), count, this.playerBars.getOrDefault(player, new LinkedHashMap<>()).size(), this.maxBar)
            );
        }, 1L, this.tick_clear_dealy * 20L);
    }

    private long loadTick_clear_dealy() {
        return Ari.C_INSTANCE.getValue("attack-boss-bar.tick_clear_dealy", FilePath.FUNCTION_CONFIG, Long.class, 30L);
    }

    private long loadClear_last_attack_record() {
        return Ari.C_INSTANCE.getValue("attack-boss-bar.clear_last_attack_record", FilePath.FUNCTION_CONFIG, Long.class, 20L);
    }

}
