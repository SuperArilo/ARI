package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.api.task.CancellableTask;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.AttackBarConfig;
import com.tty.ari.dto.state.player.AttackBossBarState;
import com.tty.ari.enumType.lang.PlaceholderPlayerDamageBar;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

public class AttackBossBarService extends StateService<AttackBossBarState> {

    public AttackBossBarService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(AttackBossBarState state) {
        return this.getAllStates().size() <= Ari.instance.getConfigurationManager().get(AttackBarConfig.class).getMaxBar() - 1;
    }

    @Override
    protected void loopExecution(AttackBossBarState state) {
        BossBar bar = state.getBar();
        if (!(state.getOwner() instanceof Player player)) {
            state.setOver(true);
            return;
        }
        if (bar == null || !player.isOnline() || player.isDead()) {
            state.setOver(true);
            return;
        }
        state.setRunning(true);
        Ari.instance.getScheduler().run(Ari.instance, i -> {
            Entity entity = Bukkit.getServer().getEntity(state.getTarget().getUniqueId());
            if (entity == null) {
                state.setOver(true);
                return;
            }
            Ari.instance.getScheduler().runAtRegion(Ari.instance, entity.getLocation(), t -> {
                if(!(entity instanceof Damageable damageable)) {
                    state.setOver(true);
                    return;
                }
                double currentHealth = damageable.getHealth();
                state.updateSaveHealth(currentHealth);
                bar.color(this.getMobBarColor(damageable));
                bar.progress(this.getTargetCurrentHealthProgress(damageable));
                bar.name(this.buildTitle(damageable));
                state.setRunning(false);
            });
        });

    }

    @Override
    protected void abortAddState(AttackBossBarState state) {

    }

    @Override
    protected void passAddState(AttackBossBarState state) {
        BossBar bossBar = BossBar.bossBar(buildTitle(state.getTarget()), this.getTargetCurrentHealthProgress(state.getTarget()), this.getMobBarColor(state.getTarget()), BossBar.Overlay.NOTCHED_10);
        state.setBar(bossBar);
        state.getOwner().showBossBar(bossBar);
    }

    @Override
    protected void onEarlyExit(AttackBossBarState state) {
    }

    @Override
    protected void onFinished(AttackBossBarState state) {
    }

    @Override
    protected void onServiceAbort(AttackBossBarState state) {
        state.getTask().cancel();
        state.setOver(true);
    }

    @Override
    public void onReload() {
        for (AttackBossBarState state : this.getAllStates()) {
            BossBar bar = state.getBar();
            if (bar != null) {
                state.getOwner().hideBossBar(bar);
            }
            CancellableTask task = state.getTask();
            if (task != null) {
                task.cancel();
            }
            state.setOver(true);
        }
    }

    private double getTargetHealthRatio(Damageable damageable) {
        if (!(damageable instanceof Attributable attr)) return 1.0;
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = Math.max(1, attribute == null ? 1 : attribute.getValue());
        return Math.clamp(damageable.getHealth(), 0, maxHealth) / maxHealth;
    }

    private Component buildTitle(Damageable entity) {
        if (!(entity instanceof Attributable attr)) return Component.empty();
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute == null ? 1 : attribute.getValue();
        double currentHealth = Math.clamp(entity.getHealth(), 0, maxHealth);
        double healthRatio = currentHealth / Math.max(1, maxHealth);
        return ConfigUtils.tAfter(
                "server.boss-bar.player-attack",
                Map.of(
                        PlaceholderPlayerDamageBar.MOB_UNRESOLVED.getType(), entity.name(),
                        PlaceholderPlayerDamageBar.MOB_CURRENT_HEALTH_UNRESOLVED.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(currentHealth))
                                .color(this.getMobHealthTextColor(healthRatio)),
                        PlaceholderPlayerDamageBar.MOB_MAX_HEALTH_UNRESOLVED.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))
                )
        );
    }

    private TextColor getMobHealthTextColor(double ratio) {
        if (ratio >= 0.7f) return TextColor.color(0x55FF55);
        if (ratio >= 0.3f) return TextColor.color(0xFFFF55);
        return TextColor.color(0xFF5555);
    }

    private BossBar.Color getMobBarColor(Damageable damageable) {
        if (!(damageable instanceof Attributable attr)) return BossBar.Color.GREEN;
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute == null ? 1 : attribute.getValue();

        double ratio = damageable.getHealth() / Math.max(1, maxHealth);
        if (ratio >= 0.7f) return BossBar.Color.GREEN;
        if (ratio >= 0.3f) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }

    private float getTargetCurrentHealthProgress(Damageable damageable) {
        return (float) Math.max(0, this.getTargetHealthRatio(damageable));
    }

}
