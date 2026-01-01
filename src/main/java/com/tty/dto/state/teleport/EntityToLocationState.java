package com.tty.dto.state.teleport;

import com.tty.lib.dto.state.TeleportState;
import com.tty.lib.enum_type.TeleportType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityToLocationState extends TeleportState {

    @Getter
    private final Location location;

    public EntityToLocationState(Entity owner, int max_count, Location location, TeleportType type) {
        super(owner, type, (owner instanceof Player p && p.isOp()) ? 0:max_count);
        this.location = location;
    }

}
