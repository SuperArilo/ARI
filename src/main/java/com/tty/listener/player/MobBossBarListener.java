package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.bar.PlayerAttackBar;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.FormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MobBossBarListener implements Listener {

    private int maxBar = 0;
    private boolean isDisabled = true;
    private final Map<Player, LinkedHashMap<Damageable, PlayerAttackBar>> playerBars = new ConcurrentHashMap<>();

    public MobBossBarListener() {
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (this.isDisabled) return;
        if (!(event.getDamager() instanceof Player player)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable mob && entity instanceof Attributable attr)) return;

        double maxHealth;
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        maxHealth = attribute != null ? attribute.getValue() : 0;

        double newHealth = mob.getHealth() - event.getFinalDamage();
        if (newHealth < 0) newHealth = 0;

        LinkedHashMap<Damageable, PlayerAttackBar> bars = this.playerBars.computeIfAbsent(player, k -> new LinkedHashMap<>());

        PlayerAttackBar bar = bars.get(mob);
        if (bar == null || bar.isRemoved()) {
            if (bar != null) {
                bar.remove(player);
            }
            bar = new PlayerAttackBar(
                    player,
                    ConfigUtils.t(
                            "server.boss-bar.player-attack",
                            Map.of(
                                    LangType.MOB.getType(), mob.name(),
                                    LangType.MOB_CURRENT_HEALTH.getType(), Component.text(FormatUtils.formatTwoDecimalPlaces(newHealth)),
                                    LangType.MOB_MAX_HEALTH.getType(), Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))
                            )
                    )
            );
            bars.put(mob, bar);
        }

        float healthRatio = (float) (newHealth / maxHealth);
        bar.setProgress(healthRatio);
        bar.setColor(getColor(healthRatio));

        this.enforceLimit(bars, player);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        if (this.isDisabled) return;
        Player player = event.getPlayer();
        LinkedHashMap<Damageable, PlayerAttackBar> bars = playerBars.remove(player);
        if (bars != null) {
            bars.values().forEach(i -> i.remove(player));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Damageable dead = event.getEntity();
        for (Map.Entry<Player, LinkedHashMap<Damageable, PlayerAttackBar>> entry : playerBars.entrySet()) {
            Player player = entry.getKey();
            LinkedHashMap<Damageable, PlayerAttackBar> bars = entry.getValue();

            PlayerAttackBar bar = bars.remove(dead);
            if (bar != null) {
                bar.remove(player);
            }
        }
    }


    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
        if (this.isDisabled) {
            this.playerBars.forEach((player, bars) ->
                    bars.values().forEach(bar -> bar.remove(player))
            );
            this.playerBars.clear();
        }

    }

    private void enforceLimit(LinkedHashMap<Damageable, PlayerAttackBar> bars, Player player) {
        while (bars.size() > this.maxBar) {
            Iterator<Map.Entry<Damageable, PlayerAttackBar>> iterator = bars.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<Damageable, PlayerAttackBar> entry = iterator.next();
                entry.getValue().remove(player);
                iterator.remove();
            }
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

}
