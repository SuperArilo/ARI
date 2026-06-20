package com.tty.ari.dto.state.player;

import com.tty.api.state.State;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;

public class PlayerChatState extends State {

    @Getter
    private final Component message;

    public PlayerChatState(Entity owner, Component message, int max_count) {
        super(owner, max_count);
        this.message = message;
    }

}
