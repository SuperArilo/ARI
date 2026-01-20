package com.tty.dto.state.teleport;

import com.tty.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerToPlayerState extends EntityToEntityState {

    @Getter
    private final String command;

    public PlayerToPlayerState(Entity owner, Entity target, int max_count, String command) {
        super(owner, TeleportType.TPA, target, (owner instanceof Player p && p.isOp()) ? 0:max_count);
        this.command = command;
    }
}
