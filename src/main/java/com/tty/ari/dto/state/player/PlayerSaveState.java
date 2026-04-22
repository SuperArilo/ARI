package com.tty.ari.dto.state.player;

import com.tty.ari.Ari;
import com.tty.api.state.AsyncState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

public class PlayerSaveState extends AsyncState {

    @Getter
    @Setter
    private long loginTime;

    public PlayerSaveState(Entity owner, long loginTime) {
        super(owner, Ari.instance.getConfig().getInt("server.save-interval", 300 * 20));
        this.loginTime = loginTime;
    }

}
