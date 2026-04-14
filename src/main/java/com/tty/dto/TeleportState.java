package com.tty.dto;

import com.tty.api.state.State;
import com.tty.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TeleportState extends State {

    @Getter
    private final TeleportType type;

    @Getter
    private final Location location;

    public TeleportState(Entity owner, TeleportType type, Location location, int max_count) {
        super(owner, max_count);
        this.location = location;
        this.type = type;
    }
}
