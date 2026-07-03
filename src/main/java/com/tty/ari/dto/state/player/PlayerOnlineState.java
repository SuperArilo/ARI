package com.tty.ari.dto.state.player;

import com.tty.api.state.AsyncState;
import org.bukkit.entity.Entity;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerOnlineState extends AsyncState {

    private final AtomicInteger standCount = new AtomicInteger(0);
    private int AFKCount;

    public PlayerOnlineState(Entity owner) {
        super(owner, Integer.MAX_VALUE);
        this.AFKCount = 5;
    }

    public void addStandCount() {
        this.standCount.set(this.standCount.get() + 1);
    }

    public void resetStandCount() {
        this.standCount.set(0);
    }

    public boolean isAFK() {
        return this.standCount.get() >= this.AFKCount;
    }

}
