package com.tty.dto.state;

import com.tty.lib.dto.state.State;
import com.tty.lib.enum_type.TeleportType;
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
