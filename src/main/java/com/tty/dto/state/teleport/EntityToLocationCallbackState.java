package com.tty.dto.state.teleport;

import com.tty.dto.state.CallbackState;
import com.tty.api.state.StateCondition;
import com.tty.enumType.TeleportType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityToLocationCallbackState extends CallbackState {


    public EntityToLocationCallbackState(Entity owner, int max_count, Location location, StateCondition customCondition, Runnable successCallback, TeleportType type) {
        super(owner, type, location, (owner instanceof Player p && p.isOp()) ? 0:max_count, customCondition, successCallback);
    }
}
