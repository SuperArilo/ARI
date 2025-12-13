package com.tty.dto.state;

import com.tty.lib.dto.State;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

public class AsyncState extends State {

    @Getter
    @Setter
    private volatile boolean isRunning;

    public AsyncState(Entity owner, int max_count) {
        super(owner, max_count);
    }
}
