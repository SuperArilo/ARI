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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.tty.listener.player.DamageTrackerListener.DAMAGE_TRACKER;

public class MobBossBarListener implements Listener {

    private int maxBar = 0;
    private boolean isDisabled = true;
    private final Map<Player, LinkedHashMap<Damageable, PlayerAttackBar>> playerBars = new ConcurrentHashMap<>();

    private CancellableTask cleanTask;

    public MobBossBarListener() {
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
        this.cleanTask = this.createCleanTask();
    }

    private void debugLog(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Damageable mob)) return;
        double finalDamage = event.getFinalDamage();
        double health = Math.max(0, mob.getHealth() - finalDamage);
        Entity directEntity = event.getDamageSource().getDirectEntity();
        Log.debug("attacker: %s, event: %s, entity: %s, damage: %s, health: %s, damageType: %s, status: %s.",
                directEntity == null ? "null":directEntity.getName(),
                event.getEventName(),
                event.getEntity().getName(),
                FormatUtils.formatTwoDecimalPlaces(event.getFinalDamage()),
                FormatUtils.formatTwoDecimalPlaces(health),
                event.getCause().name(),
                health == 0F ? "death":"living")
        ;
    }

    private boolean isBoss(Damageable damageable) {
        return (damageable instanceof Boss);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (this.isDisabled) return;

        Player attacker;
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        //攻击者和受害者必须有一个是属于玩家类
        if (damager instanceof Player player) {
            attacker = player;
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            attacker = player;
        } else return;

        if (!(entity instanceof Damageable victim)) return;

        //如果是自己造成的伤害不显示
        if (victim == attacker) return;
        if (this.isBoss(victim)) return;

        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records.isEmpty()) return;
        LastDamageTracker.DamageRecord last = records.getLast();
        if (last.hash() == Objects.hash(event)) {
            Log.debug("attacker %s to victim %s bar already exists. type: %s. skip....",
                    attacker.getName(),
                    victim.getName(),
                    event.getCause().name()
            );
            return;
        }

        this.updateBar(event, victim, attacker);
        this.debugLog(event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.isDisabled) return;
        Entity victim = event.getEntity();

        if (!(victim instanceof Damageable victimDamageable && victim instanceof Attributable)) return;
        if (this.isBoss(victimDamageable)) return;
        List<LastDamageTracker.DamageRecord> re = DAMAGE_TRACKER.getRecords(victim);
        if (re.isEmpty()) return;
        if (!(re.getLast().damager() instanceof Player player)) return;

        //如果是自己造成的伤害不显示
        if(victimDamageable == player) return;

        this.updateBar(event, victimDamageable, player);
        this.debugLog(event);
    }

    /**
     * 更新玩家 bar
     * @param event 事件
     * @param damageable 被玩家攻击的对象
     * @param player 玩家
     */
    private void updateBar(EntityDamageEvent event, Damageable damageable, Player player) {
        if (!(damageable instanceof Attributable attr)) return;
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute == null ? 1 : attribute.getValue();

        double currentHealth = Math.max(0, damageable.getHealth() - event.getFinalDamage());
        LinkedHashMap<Damageable, PlayerAttackBar> bars = playerBars.computeIfAbsent(player, k -> new LinkedHashMap<>());

        while (bars.size() >= this.maxBar) {
            Map.Entry<Damageable, PlayerAttackBar> oldest = bars.entrySet().iterator().next();
            oldest.getValue().remove(player);
            bars.remove(oldest.getKey());
        }

        PlayerAttackBar bar = bars.get(damageable);

        double healthRatio = (currentHealth / Math.max(1, maxHealth));

        TextComponent t = ConfigUtils.t(
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
        if (Float.compare(bar.getProgress(), (float) healthRatio) != 0) {
            bar.setProgress(Float.parseFloat(FormatUtils.formatTwoDecimalPlaces(Math.max(0, healthRatio))));
            bar.setColor(this.getMobBarColor(healthRatio));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (this.isDisabled) return;
        this.removePlayerRecord(event.getPlayer());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Damageable deadEntity = event.getEntity();

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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.isDisabled) return;
        this.removePlayerRecord(event.getEntity());
    }

    private void removePlayerRecord(Player player) {
        LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.remove(player);
        if (bars != null) {
            bars.values().forEach(bar -> bar.remove(player));
        }
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.maxBar = getMaxBar();
        this.isDisabled = isDisabled();
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
                    if (bar.isRemoved() || lastAttackTs == 0 || (now - lastAttackTs) > 20_000L) {
                        bar.remove(player);
                        it.remove();
                        removedCountByPlayer.merge(player, 1, Integer::sum);
                    }
                }
            }
            removedCountByPlayer.forEach((player, count) ->
                    Log.debug("mob bar expired: player=%s, removedEntities=%s, current_bar_count=%s, max_bar_count=%s",
                            player.getName(), count, this.playerBars.getOrDefault(player, new LinkedHashMap<>()).size(), this.maxBar)
            );
        }, 1L, 30 * 20L);
    }


}
