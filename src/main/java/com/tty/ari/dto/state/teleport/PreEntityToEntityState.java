package com.tty.ari.dto.state.teleport;

import com.tty.ari.dto.TeleportState;
import com.tty.ari.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class PreEntityToEntityState extends TeleportState {

    @Getter
    private final Entity target;
    @Getter
    private final TeleportType type;

    public PreEntityToEntityState(Entity owner, Entity target, TeleportType type, int max_count) {
        super(owner, type, target.getLocation(), max_count);
        this.target = target;
        this.type = type;
    }
}
