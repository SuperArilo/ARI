package com.tty.ari.dto.state.teleport;

import com.tty.ari.dto.TeleportState;
import com.tty.ari.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class EntityToEntityState extends TeleportState {

    @Getter
    private final Entity target;

    public EntityToEntityState(Entity owner, TeleportType type, Entity target, int max_count) {
        super(owner, type, target.getLocation(), max_count);
        this.target = target;
    }
}
