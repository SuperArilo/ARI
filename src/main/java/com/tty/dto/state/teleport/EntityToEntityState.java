package com.tty.dto.state.teleport;

import com.tty.dto.TeleportState;
import com.tty.enumType.TeleportType;
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
