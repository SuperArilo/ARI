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
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobBossBarListener implements Listener {

    private static final long DOT_ATTacker_TTL_MS = 3_000L;     // 用于 DOT 判定（短 TTL）

    private int maxBar = 0;
    private boolean isDisabled = true;
    private final Map<Player, LinkedHashMap<Damageable, PlayerAttackBar>> playerBars = new ConcurrentHashMap<>();
    private final LastDamageTracker tracker = new LastDamageTracker();

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
        Log.debug("event: %s, entity: %s, damage: %s, health: %s, damageType: %s, status: %s.",
                event.getEventName(),
                event.getEntity().getName(),
                FormatUtils.formatTwoDecimalPlaces(event.getFinalDamage()),
                FormatUtils.formatTwoDecimalPlaces(health),
                event.getDamageSource().getDamageType().getTranslationKey(),
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
        if (!(victim instanceof Damageable victimDamageable && victim instanceof Attributable attr)) return;
        if (this.isBoss(victimDamageable)) return;

        // 更新显示
        this.updateBar(event, victimDamageable, attr, attacker);
        this.debugLog(event);

        // 记录此次伤害（LastDamageTracker 用于后续 DOT / 清理）
        ItemStack weapon = null;
        if (damager instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) weapon = eq.getItemInMainHand();
        }

        this.tracker.addRecord(
                victimDamageable,
                attacker,
                event.getFinalDamage(),
                damager instanceof Projectile,
                weapon
        );
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.isDisabled) return;
        Entity victim = event.getEntity();
        if (!(victim instanceof Damageable mob && victim instanceof Attributable attr)) return;
        if (this.isBoss(mob)) return;
        switch (event.getCause()) {
            case FIRE_TICK, FIRE, POISON, WITHER, MAGIC, PROJECTILE, ENTITY_EXPLOSION -> {
                List<LastDamageTracker.DamageRecord> records = this.tracker.getRecords(victim);
                if (records.isEmpty()) return;
                this.debugLog(event);
                long now = System.currentTimeMillis();
                Player attackerPlayer = null;

                // 从后向前查找最近能归因到玩家的记录
                for (int i = records.size() - 1; i >= 0; i--) {
                    LastDamageTracker.DamageRecord r = records.get(i);
                    if (now - r.timestamp() > DOT_ATTacker_TTL_MS) {
                        records.remove(i);
                        continue;
                    }
                    Entity damager = r.damager();
                    if (damager instanceof Player p) {
                        attackerPlayer = p;
                        break;
                    }
                    if (r.isProjectile() && damager instanceof Projectile proj) {
                        Object shooter = proj.getShooter();
                        if (shooter instanceof Player sp) {
                            attackerPlayer = sp;
                            break;
                        }
                    }
                }

                if (attackerPlayer == null) return;
                if (!attackerPlayer.isOnline()) return;

                Player finalAttackerPlayer = attackerPlayer;
                Lib.Scheduler.runAtEntity(Ari.instance, attackerPlayer, i -> this.updateBar(event, mob, attr, finalAttackerPlayer), null);
            }
        }
    }




    private void updateBar(EntityDamageEvent event, Damageable mob, Attributable attr, Player attacker) {
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute == null ? 1 : attribute.getValue();

        double currentHealth = Math.max(0, mob.getHealth() - event.getFinalDamage());
        Log.debug(String.valueOf(currentHealth));
        LinkedHashMap<Damageable, PlayerAttackBar> bars = playerBars.computeIfAbsent(attacker, k -> new LinkedHashMap<>());

        while (bars.size() >= this.maxBar) {
            Map.Entry<Damageable, PlayerAttackBar> oldest = bars.entrySet().iterator().next();
            oldest.getValue().remove(attacker);
            bars.remove(oldest.getKey());
        }

        PlayerAttackBar bar = bars.get(mob);

        double healthRatio = (currentHealth / Math.max(1, maxHealth));

        TextComponent t = ConfigUtils.t(
                "server.boss-bar.player-attack",
                Map.of(
                        LangType.MOB.getType(), mob.name(),
                        LangType.MOB_CURRENT_HEALTH.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(currentHealth)).color(this.getMobHealthTextColor(healthRatio)),
                        LangType.MOB_MAX_HEALTH.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))
                )
        );

        if (bar == null || bar.isRemoved()) {
            if (bar != null) {
                bar.remove(attacker);
            }
            bar = new PlayerAttackBar(attacker, t, Float.parseFloat(FormatUtils.formatTwoDecimalPlaces(healthRatio)), this.getMobBarColor(healthRatio));
            bars.put(mob, bar);
        } else {
            bar.setName(t);
        }

        bar.setProgress(Float.parseFloat(FormatUtils.formatTwoDecimalPlaces(Math.max(0, healthRatio))));
        bar.setColor(this.getMobBarColor(healthRatio));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        if (this.isDisabled) return;
        this.removePlayerRecord(event.getPlayer());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Damageable dead = event.getEntity();
        var affectedPlayers = new LinkedHashMap<Player, PlayerAttackBar>();
        playerBars.forEach((player, bars) -> {
            PlayerAttackBar bar = bars.get(dead);
            if (bar != null) {
                affectedPlayers.put(player, bar);
            }
        });
        Lib.Scheduler.runAtEntity(Ari.instance, dead, i -> {
            affectedPlayers.forEach((player, bar) -> {
                LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.get(player);
                if (bars != null) bars.remove(dead);
            });
            this.tracker.clearRecords(dead);
        }, null);
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
        this.tracker.clearDamagerRecords(player);
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.maxBar = getMaxBar();
        this.isDisabled = isDisabled();
        if (this.isDisabled) {
            this.playerBars.forEach((player, bars) -> bars.values().forEach(bar -> bar.remove(player)));
            this.playerBars.clear();
            this.tracker.removeAll();
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
            Set<Entity> victims = this.tracker.getVictimsSnapshot();
            for (Entity e : victims) {
                if (!(e instanceof Damageable mob)) continue;
                long lastTs = this.tracker.getLastTimestamp(mob);
                // 如果没有记录或超时（半分钟无新记录），则清理
                if (lastTs == 0L || (now - lastTs) > 10_000L) {
                    Iterator<Map.Entry<Player, LinkedHashMap<Damageable, PlayerAttackBar>>> playerIt =
                            this.playerBars.entrySet().iterator();
                    while (playerIt.hasNext()) {
                        Map.Entry<Player, LinkedHashMap<Damageable, PlayerAttackBar>> playerEntry = playerIt.next();
                        Player player = playerEntry.getKey();
                        LinkedHashMap<Damageable, PlayerAttackBar> bars = playerEntry.getValue();
                        PlayerAttackBar bar = bars.remove(mob);
                        if (bar != null) {
                            bar.remove(player);
                            removedCountByPlayer.merge(player, 1, Integer::sum);
                        }
                        if (bars.isEmpty()) {
                            playerIt.remove();
                        }
                    }
                    this.tracker.clearRecords(mob);
                }
            }
            removedCountByPlayer.forEach((player, count) ->
                    Log.debug("mob bar expired: player=%s, removedEntities=%s, current_bar_count=%s, max_bar_count=%s", player.getName(), count, this.playerBars.getOrDefault(player, new LinkedHashMap<>()).size(), this.maxBar)
            );
        }, 1L, 10 * 20L);
    }


}
