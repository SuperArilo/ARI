package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.bar.PlayerAttackBar;
import com.tty.lib.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.Lib;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.FormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MobBossBarListener implements Listener {

    private static final long ATTACKER_TTL_MS = 3_000L;

    private int maxBar = 0;
    private boolean isDisabled = true;
    private final Map<Player, LinkedHashMap<Damageable, PlayerAttackBar>> playerBars = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Damageable, AttackerRecord> lastAttackerMap = new ConcurrentHashMap<>();

    public MobBossBarListener() {
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (this.isDisabled) return;

        Player attacker = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player shooter) attacker = shooter;
        }
        if (attacker == null) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable mob && entity instanceof Attributable attr)) return;

        this.updateBar(event, mob, attr, attacker);
        this.lastAttackerMap.put(mob, new AttackerRecord(attacker, System.currentTimeMillis()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.isDisabled) return;

        if (!(event.getEntity() instanceof Damageable mob && event.getEntity() instanceof Attributable attr)) return;

        switch (event.getCause()) {
            case FIRE_TICK, FIRE, POISON, WITHER, MAGIC -> {
                AttackerRecord rec = lastAttackerMap.get(mob);
                if (rec == null) return;
                if (System.currentTimeMillis() - rec.timestamp > ATTACKER_TTL_MS) {
                    this.lastAttackerMap.remove(mob);
                    return;
                }

                Player attacker = rec.player;
                if (attacker == null || !attacker.isOnline()) return;

                this.updateBar(event, mob, attr, attacker);
            }
        }
    }

    private void updateBar(EntityDamageEvent event, Damageable mob, Attributable attr, Player attacker) {

        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute == null ? 0:attribute.getValue();
        double newHealth = Math.max(0, mob.getHealth() - event.getFinalDamage());

        LinkedHashMap<Damageable, PlayerAttackBar> bars = playerBars.computeIfAbsent(attacker, k -> new LinkedHashMap<>());
        PlayerAttackBar bar = bars.get(mob);

        TextComponent t = ConfigUtils.t(
                "server.boss-bar.player-attack",
                Map.of(
                        LangType.MOB.getType(), mob.name(),
                        LangType.MOB_CURRENT_HEALTH.getType(), Component.text(FormatUtils.formatTwoDecimalPlaces(newHealth)),
                        LangType.MOB_MAX_HEALTH.getType(), Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))
                )
        );

        float healthRatio = (float) (newHealth / maxHealth);

        if (bar == null || bar.isRemoved()) {
            if (bar != null) bar.remove(attacker);
            bar = new PlayerAttackBar(attacker, t, healthRatio, this.getColor(healthRatio));
            bars.put(mob, bar);
        } else {
            bar.setName(t);
        }

        if (bar.getProgress() != healthRatio) {
            bar.setProgress(healthRatio);
            bar.setColor(this.getColor(healthRatio));
        }
        this.enforceLimit(bars, attacker);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        if (this.isDisabled) return;
        Player player = event.getPlayer();

        LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.remove(player);
        if (bars != null) bars.values().forEach(i -> i.remove(player));

        this.lastAttackerMap.entrySet().removeIf(e -> e.getValue().player.equals(player));
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
        Lib.Scheduler.run(Ari.instance, i -> {
            affectedPlayers.forEach((player, bar) -> {
                LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.get(player);
                if (bars != null) bars.remove(dead);
            });
            this.lastAttackerMap.remove(dead);
        });
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.maxBar = getMaxBar();
        this.isDisabled = isDisabled();
        if (this.isDisabled) {
            this.playerBars.forEach((player, bars) -> bars.values().forEach(bar -> bar.remove(player)));
            this.playerBars.clear();
            this.lastAttackerMap.clear();
        }
    }

    private void enforceLimit(LinkedHashMap<Damageable, PlayerAttackBar> bars, Player player) {
        while (bars.size() > this.maxBar) {
            Map.Entry<Damageable, PlayerAttackBar> oldest = bars.entrySet().iterator().next();
            oldest.getValue().remove(player);
            bars.remove(oldest.getKey());
        }
    }

    private BossBar.Color getColor(float ratio) {
        if (ratio >= 0.7f) return BossBar.Color.GREEN;
        if (ratio >= 0.3f) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }

    private boolean isDisabled() {
        return !Ari.C_INSTANCE.getValue("attack-boss-bar.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

    private int getMaxBar() {
        return Ari.C_INSTANCE.getValue("attack-boss-bar.max-bar", FilePath.FUNCTION_CONFIG, Integer.class, 1);
    }

    private record AttackerRecord(Player player, long timestamp) {}
}
