package com.tty.ari.dto.state;

import com.tty.api.state.State;
import com.tty.ari.enumType.TeleportType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class CooldownState extends State {

    @Getter
    private final TeleportType type;

    public CooldownState(Entity owner, int max_count, TeleportType type) {
        super(owner, max_count);
        this.type = type;
    }
}
