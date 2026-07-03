package com.tty.ari.listener.player;

import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerOnlineState;
import com.tty.ari.states.PlayerOnlineService;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class PlayerAFKStatusListener implements Listener {

    @EventHandler
    public void onHurt(EntityDamageEvent event) {
        if (!((event.getEntity()) instanceof Player player)) return;
        this.check(player, event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.check(event.getPlayer(), event);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        this.check(event.getPlayer(), event);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        this.check(event.getPlayer(), event);
    }

    @EventHandler
    public void onBucketOne(PlayerBucketEmptyEvent event) {
        this.check(event.getPlayer(), event);
    }

    @EventHandler
    public void onBucketTwo(PlayerBucketFillEvent event) {
        this.check(event.getPlayer(), event);
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent event) {
        this.check(event.getPlayer(), event);
    }

    private void check(Player player, Cancellable cancellable) {
        for (PlayerOnlineState state : Ari.instance.getStatusManager().get(PlayerOnlineService.class).getStates(player)) {
            if (state.getOwner().equals(player) && state.isAFK()) {
                cancellable.setCancelled(true);
            }
        }
    }
}
