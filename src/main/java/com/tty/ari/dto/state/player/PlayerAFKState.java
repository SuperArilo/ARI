package com.tty.ari.dto.state.player;

import com.tty.api.state.AsyncState;
import com.tty.ari.Ari;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerAFKState extends AsyncState {

    private final AtomicInteger standCount = new AtomicInteger(0);
    private final int AFKCount;

    @Getter
    @Setter
    private boolean isSent = false;

    public PlayerAFKState(Entity owner) {
        super(owner, Integer.MAX_VALUE);
        this.AFKCount = Ari.instance.getConfig().getInt("server.afk.delay", 30);
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
