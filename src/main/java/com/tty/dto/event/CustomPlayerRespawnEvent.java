package com.tty.dto.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CustomPlayerRespawnEvent extends PlayerEvent implements Cancellable {
    @Getter
    private final static HandlerList handlerList = new HandlerList();
    @Getter
    @Setter
    private Location respawnLocation;
    @Setter
    @Getter
    private Location deathLocation;
    private boolean isCancelled = false;
    public CustomPlayerRespawnEvent(@NotNull Player who, @NotNull Location respawnLocation, @NotNull Location deathLocation) {
        super(who);
        this.respawnLocation = respawnLocation;
        this.deathLocation = deathLocation;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
