package com.tty.ari.dto.state.player;

import com.tty.api.state.AsyncState;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class PlayerMorphState extends AsyncState {

    @Getter
    private final EntityType type;

    public PlayerMorphState(Entity owner, EntityType type) {
        super(owner, Integer.MAX_VALUE);
        this.type = type;
    }

}
