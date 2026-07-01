package com.tty.ari.dto.state.player;

import com.tty.api.state.State;
import com.tty.ari.Ari;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

public class PlayerOnlineState extends State {

    @Getter
    @Setter
    private long loginTime;

    public PlayerOnlineState(Entity owner, long loginTime) {
        super(owner, Ari.instance.getConfig().getInt("server.save-interval", 300 * 20));
        this.loginTime = loginTime;
    }

}
