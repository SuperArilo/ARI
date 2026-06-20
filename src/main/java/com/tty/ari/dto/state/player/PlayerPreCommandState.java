package com.tty.ari.dto.state.player;

import com.tty.api.state.State;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class PlayerPreCommandState extends State {

    @Getter
    private final String mainCommand;

    public PlayerPreCommandState(Entity owner, String mainCommand, int max_count) {
        super(owner, max_count);
        this.mainCommand = mainCommand;
    }

}
