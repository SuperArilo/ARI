package com.tty.ari.dto.state.player;

import com.tty.api.state.AsyncState;
import com.tty.ari.Ari;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerMorphState extends AsyncState {

    @Getter
    private EntityType type;

    public PlayerMorphState(Entity owner, EntityType type) {
        super(owner, Integer.MAX_VALUE);
        this.type = type;
    }

    public void change(EntityType type) {
        if (!(this.getOwner() instanceof Player player) || !player.isOnline()) return;
        if (this.type.equals(type)) return;
        this.type = type;
        Ari.instance.getScheduler().run(i -> Bukkit.getServer().getPluginManager().callEvent(new PlayerMorphTypeChangeEvent(player, type)));
    }

    public static class PlayerMorphTypeChangeEvent extends Event {

        @Getter
        private final static HandlerList handlerList = new HandlerList();
        @Getter
        private final Player morpher;
        @Getter
        private final EntityType newType;

        public PlayerMorphTypeChangeEvent(Player morpher, EntityType newType) {
            this.morpher = morpher;
            this.newType = newType;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return handlerList;
        }

    }

}
