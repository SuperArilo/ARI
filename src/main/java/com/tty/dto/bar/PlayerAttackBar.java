package com.tty.dto.bar;

import com.tty.Ari;
import com.tty.dto.BaseBossBar;
import com.tty.lib.Lib;
import com.tty.lib.task.CancellableTask;
import org.bukkit.entity.Player;

public class PlayerAttackBar extends BaseBossBar {

    private CancellableTask task;


    public PlayerAttackBar(Player player) {
        super(player);
        this.autoRemove();
    }

    private void autoRemove() {
        this.task = Lib.Scheduler.runAtEntityLater(
                Ari.instance,
                this.player,
                i -> this.remove(),
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
    public void remove() {
        super.remove();
        this.cancelTask();
    }

}
