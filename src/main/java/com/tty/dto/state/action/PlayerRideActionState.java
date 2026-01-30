package com.tty.dto.state.action;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerRideActionState extends PlayerActionState {

    @Getter
    private final Player beRidePlayer;

    public PlayerRideActionState(Entity owner, Player beRidePlayer) {
        super(owner);
        this.beRidePlayer = beRidePlayer;
    }

}
