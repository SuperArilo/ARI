package com.tty.dto.state.player;

import com.tty.dto.state.AsyncState;
import com.tty.lib.task.CancellableTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

public class PlayerSaveState extends AsyncState {

    @Getter
    @Setter
    private long loginTime;

    @Getter
    @Setter
    private CancellableTask task;

    public PlayerSaveState(Entity owner, long loginTime) {
        super(owner, Integer.MAX_VALUE);
        this.loginTime = loginTime;
    }

}
