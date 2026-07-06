package com.tty.ari.listener.player;

import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.states.PlayerAFKService;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.*;

public class PlayerAFKStatusListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
        if (this.isAFK(event.getPlayer())) {
            this.reset(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        boolean lookChanged = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        boolean posChanged = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();

        if (this.isAFK(player)) {
            if (lookChanged) {
                this.reset(player);
                if (posChanged) {
                    event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
                }
            } else if (posChanged) {
                event.setCancelled(true);
                event.setTo(from);
            }
        } else {
            if (posChanged || lookChanged) {
                this.reset(player);
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (this.isAFK(event.getPlayer())) {
            this.reset(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (this.isAFK(event.getPlayer())) {
            this.reset(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (this.isAFK(event.getPlayer())) {
            this.reset(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (this.isAFK(event.getPlayer())) {
            this.reset(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (this.isAFK(event.getPlayer())) {
            this.reset(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (this.isAFK(event.getPlayer())) this.reset(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (this.isAFK(event.getPlayer())) this.reset(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (this.isAFK(event.getPlayer())) this.reset(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (this.isAFK(event.getPlayer())) this.reset(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHurt(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && this.isAFK(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) return;
        if (this.isAFK(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isAFK(Player player) {
        for (PlayerAFKState state : Ari.instance.getStatusManager().get(PlayerAFKService.class).getStates(player)) {
            if (state.getOwner().equals(player) && state.isAFK()) {
                return true;
            }
        }
        return false;
    }

    private void reset(Player player) {
        for (PlayerAFKState state : Ari.instance.getStatusManager().get(PlayerAFKService.class).getStates(player)) {
            if (state.getOwner().equals(player)) {
                state.resetStandCount();
            }
        }
    }

}