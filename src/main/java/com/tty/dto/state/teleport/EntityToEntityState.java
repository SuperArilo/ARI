package com.tty.dto.state.teleport;

import com.tty.lib.dto.state.TeleportState;
import com.tty.lib.enum_type.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class EntityToEntityState extends TeleportState {

    @Getter
    private final Entity target;

    public EntityToEntityState(Entity owner, TeleportType type, Entity target, int max_count) {
        super(owner, type, max_count);
        this.target = target;
    }
}
