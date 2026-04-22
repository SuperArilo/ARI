package com.tty.ari.dto.state.teleport;

import com.tty.ari.dto.TeleportState;
import com.tty.ari.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityToLocationState extends TeleportState {

    @Getter
    private final Location location;

    public EntityToLocationState(Entity owner, int max_count, Location location, TeleportType type) {
        super(owner, type, location, (owner instanceof Player p && p.isOp()) ? 0:max_count);
        this.location = location;
    }

}
