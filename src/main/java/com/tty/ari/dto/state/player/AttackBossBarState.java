package com.tty.ari.dto.state.player;

import com.tty.api.state.AsyncState;
import com.tty.api.task.CancellableTask;
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
    private volatile CancellableTask task;

    public AttackBossBarState(Entity owner, @NotNull Damageable target) {
        super(owner, Integer.MAX_VALUE);
        this.target = target;
        this.saveHealth = target.getHealth();
        this.task = this.createTask();
    }

    public void updateSaveHealth(double newHealth) {
        this.saveHealth = newHealth;
    }

    public void setDamage(float damage) {
        this.damage = damage;
        if (this.task != null) {
            this.task.cancel();
            this.task = this.createTask();
        }
    }

    private synchronized CancellableTask createTask() {
        return Ari.instance.getScheduler().runAtRegionLater(this.getOwner().getLocation(), i -> {
            if (this.bar != null) {
                this.getOwner().hideBossBar(Objects.requireNonNull(this.bar));
                this.setBar(null);
            }
            this.setOver(true);
            this.task = null;
        }, 30L);
    }

}
