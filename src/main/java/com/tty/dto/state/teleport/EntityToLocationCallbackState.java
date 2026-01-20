package com.tty.dto.state.teleport;

import com.tty.dto.state.CallbackState;
import com.tty.lib.dto.state.StateCondition;
import com.tty.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityToLocationCallbackState extends CallbackState {

    @Getter
    private final Location location;

    public EntityToLocationCallbackState(Entity owner, int max_count, Location location, StateCondition customCondition, Runnable successCallback, TeleportType type) {
        super(owner, type, (owner instanceof Player p && p.isOp()) ? 0:max_count, customCondition, successCallback);
        this.location = location;
    }
}
