package com.tty.dto.state.teleport;

import com.tty.lib.dto.state.TeleportState;
import com.tty.lib.enum_type.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class PreEntityToEntityState extends TeleportState {

    @Getter
    private final Entity target;
    @Getter
    private final TeleportType type;

    public PreEntityToEntityState(Entity owner, Entity target, TeleportType type, int max_count) {
        super(owner, type, max_count);
        this.target = target;
        this.type = type;
    }
}
