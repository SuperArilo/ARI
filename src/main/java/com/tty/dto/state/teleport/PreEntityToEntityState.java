package com.tty.dto.state.teleport;

import com.tty.dto.TeleportState;
import com.tty.enumType.TeleportType;
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
