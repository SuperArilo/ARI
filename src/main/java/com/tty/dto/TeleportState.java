package com.tty.dto;

import com.tty.lib.dto.state.State;
import com.tty.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class TeleportState extends State {

    @Getter
    private final TeleportType type;

    public TeleportState(Entity owner, TeleportType type, int max_count) {
        super(owner, max_count);
        this.type = type;
    }
}
