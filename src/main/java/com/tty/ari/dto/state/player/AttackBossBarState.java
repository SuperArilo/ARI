package com.tty.ari.dto.state.player;

import com.tty.api.state.AsyncState;
import com.tty.api.scheduler.RunTask;
import com.tty.ari.Ari;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AttackBossBarState extends AsyncState {

    @NotNull
    @Getter
    private volatile Damageable target;

    @Setter
    @Getter
    @Nullable
    private BossBar bar;

    @Getter
    private volatile float damage = 0;

    @Getter
    private double saveHealth;

    @Getter
    private volatile RunTask runTask;

    public AttackBossBarState(Entity owner, @NotNull Damageable target) {
        super(owner, Integer.MAX_VALUE);
        this.target = target;
        this.saveHealth = target.getHealth();
        this.runTask = this.createTask();
    }

    public void updateSaveHealth(double newHealth) {
        this.saveHealth = newHealth;
    }

    public void setDamage(float damage) {
        this.damage = damage;
        if (this.runTask != null) {
            this.runTask.cancel();
            this.runTask = this.createTask();
        }
    }

    private synchronized RunTask createTask() {
        return Ari.instance.getScheduler().runAtRegionLater(this.getOwner().getLocation(), i -> {
            if (i.isCancelled()) return;
            if (this.bar != null) {
                this.getOwner().hideBossBar(Objects.requireNonNull(this.bar));
                this.setBar(null);
            }
            this.setOver(true);
            this.runTask = null;
        }, 40L);
    }

}
