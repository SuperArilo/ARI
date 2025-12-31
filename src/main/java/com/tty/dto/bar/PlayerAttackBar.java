package com.tty.dto.bar;

import com.tty.Ari;
import com.tty.dto.BaseBossBar;
import com.tty.lib.Lib;
import com.tty.lib.task.CancellableTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerAttackBar extends BaseBossBar {

    private CancellableTask task;
    private final Player player;


    public PlayerAttackBar(Player player) {
        super();
        this.player = player;
        this.autoRemove();
        super.show(player);
    }

    public PlayerAttackBar(Player player, Component component, float progress) {
        super(component, progress);
        this.player = player;
        this.autoRemove();
        super.show(player);
    }

    private void autoRemove() {
        this.task = Lib.Scheduler.runAtEntityLater(
                Ari.instance,
                this.player,
                i -> this.remove(this.player),
                null ,
                30L);
    }

    private void cancelTask() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    @Override
    public void setProgress(float value) {
        super.setProgress(value);
        this.cancelTask();
        this.autoRemove();
    }

    @Override
    public void remove(@NotNull Audience audience) {
        super.remove(audience);
        this.cancelTask();
    }

}
