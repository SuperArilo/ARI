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
        if (attribute != null) {
            maxHealth = attribute.getValue();
        } else {
            maxHealth = 0;
        }
        double newHealth = mob.getHealth() - event.getFinalDamage();
        if (newHealth < 0) newHealth = 0;

        LinkedHashMap<Damageable, PlayerAttackBar> bars =
                playerBars.computeIfAbsent(player, k -> new LinkedHashMap<>());

        PlayerAttackBar bar = bars.compute(mob, (m, existing) -> {
            if (existing == null || existing.removed) {
                if (existing != null) existing.remove();
                return new PlayerAttackBar(player);
            }
            return existing;
        });

        float healthRatio = (float) (newHealth / maxHealth);

        bar.setProgress(healthRatio);
        bar.setColor(getColor(healthRatio));
        bar.setName(ConfigUtils.t(
                "server.boss-bar.player-attack",
                Map.of(
                        LangType.MOB.getType(), mob.name(),
                        LangType.MOB_CURRENT_HEALTH.getType(), Component.text(FormatUtils.formatTwoDecimalPlaces(newHealth)),
                        LangType.MOB_MAX_HEALTH.getType(), Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))
                )
        ));

        this.enforceLimit(bars);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        if (this.isDisabled) return;
        Player player = event.getPlayer();
        LinkedHashMap<Damageable, PlayerAttackBar> bars = playerBars.remove(player);
        if (bars != null) {
            bars.values().forEach(PlayerAttackBar::remove);
        }
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.maxBar = this.getMaxBar();
        this.isDisabled = this.isDisabled();
    }

    private void enforceLimit(LinkedHashMap<Damageable, PlayerAttackBar> bars) {
        while (bars.size() > this.maxBar) {
            Iterator<Map.Entry<Damageable, PlayerAttackBar>> iterator = bars.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<Damageable, PlayerAttackBar> entry = iterator.next();
                entry.getValue().remove();
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
