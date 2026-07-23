package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.api.scheduler.RunTask;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.AttackBarConfig;
import com.tty.ari.dto.state.player.AttackBossBarState;
import com.tty.ari.enumType.lang.PlaceholderPlayerDamageBar;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Consumer;

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
        Damageable target = state.getTarget();

        Ari.instance.getScheduler().run(g -> {
            Consumer<RunTask> consumer = task -> {
                double effectiveHealth = getEffectiveHealth(target);
                state.updateSaveHealth(effectiveHealth);
                bar.color(this.getMobBarColor(target));
                bar.progress(this.getTargetCurrentHealthProgress(target));
                bar.name(this.buildTitle(target));
                state.setRunning(false);
            };
            if (!target.isDead() && target.isValid()) {
                Ari.instance.getScheduler().runAtEntity(target, consumer, () -> state.setOver(true));
            } else {
                Ari.instance.getScheduler().runAtRegion(target.getLocation(), consumer);
            }
        });
    }

    @Override
    protected void abortAddState(AttackBossBarState state) {
    }

    @Override
    protected void passAddState(AttackBossBarState state) {
        BossBar bossBar = BossBar.bossBar(
                buildTitle(state.getTarget()),
                this.getTargetCurrentHealthProgress(state.getTarget()),
                this.getMobBarColor(state.getTarget()),
                BossBar.Overlay.NOTCHED_10
        );
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
        state.getRunTask().cancel();
        state.setOver(true);
    }

    @Override
    public void onReload() {
        for (AttackBossBarState state : this.getAllStates()) {
            BossBar bar = state.getBar();
            if (bar != null) {
                state.getOwner().hideBossBar(bar);
            }
            RunTask runTask = state.getRunTask();
            if (runTask != null) {
                runTask.cancel();
            }
            state.setOver(true);
        }
    }

    private double getMaxHealth(Damageable damageable) {
        if (!(damageable instanceof Attributable attr)) return 1.0;
        AttributeInstance attribute = attr.getAttribute(Attribute.MAX_HEALTH);
        return attribute == null ? 1.0 : attribute.getValue();
    }

    private double getEffectiveHealth(Damageable damageable) {
        return damageable.getHealth() + damageable.getAbsorptionAmount();
    }

    private double getEffectiveHealthRatio(Damageable damageable) {
        double effective = getEffectiveHealth(damageable);
        double max = getMaxHealth(damageable);
        return Math.min(effective, max) / Math.max(1, max);
    }

    private float getTargetCurrentHealthProgress(Damageable damageable) {
        return (float) getEffectiveHealthRatio(damageable);
    }

    private BossBar.Color getMobBarColor(Damageable damageable) {
        double ratio = getEffectiveHealthRatio(damageable);
        if (ratio >= 0.7f) return BossBar.Color.GREEN;
        if (ratio >= 0.3f) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }

    private Component buildTitle(Damageable entity) {
        if (!(entity instanceof Attributable)) return Component.empty();
        double maxHealth = getMaxHealth(entity);
        double currentHealth = Math.clamp(entity.getHealth(), 0, maxHealth);
        double absorption = entity.getAbsorptionAmount();
        double effectiveHealth = currentHealth + absorption;
        double effectiveRatio = Math.min(effectiveHealth, maxHealth) / Math.max(1, maxHealth);

        Component health;
        if (absorption > 0) {
            health = Component.empty()
                    .append(Component.text(FormatUtils.formatTwoDecimalPlaces(currentHealth)))
                    .append(Component.text(" + " + FormatUtils.formatTwoDecimalPlaces(absorption)).color(TextColor.color(0xFFFF55)));
        } else {
            health = Component.text(FormatUtils.formatTwoDecimalPlaces(currentHealth));
        }
        health = health.color(getMobHealthTextColor(effectiveRatio));

        return ConfigUtils.tAfter("server.boss-bar.player-attack", Map.of(
                        PlaceholderPlayerDamageBar.MOB_UNRESOLVED.getType(), entity.name(),
                        PlaceholderPlayerDamageBar.MOB_CURRENT_HEALTH_UNRESOLVED.getType(), health,
                        PlaceholderPlayerDamageBar.MOB_MAX_HEALTH_UNRESOLVED.getType(),
                        Component.text(FormatUtils.formatTwoDecimalPlaces(maxHealth))));
    }

    private TextColor getMobHealthTextColor(double ratio) {
        if (ratio >= 0.7f) return TextColor.color(0x55FF55);
        if (ratio >= 0.3f) return TextColor.color(0xFFFF55);
        return TextColor.color(0xFF5555);
    }
}