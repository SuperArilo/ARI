package com.tty.dto.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OnZakoSavedEvent extends Event {

    @Getter
    private final static HandlerList handlerList = new HandlerList();

    @Getter
    private final Player player;

    public OnZakoSavedEvent(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
