package com.tty.ari.listener.player;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.states.PlayerAFKService;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

public class PlayerAFKStatusListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPush(EntityPushedByEntityAttackEvent event) {
        if (event.getEntity() instanceof Player player && this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        boolean lookChange = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        boolean posChange = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();

        if (this.isAFK(player)) {
            if (lookChange || posChange) {
                this.reset(player);
            }
            if (posChange) {
                event.setCancelled(true);
                event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
            }
        } else {
            if (posChange || lookChange) {
                this.reset(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickItem(PlayerPickItemEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupExperience(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();
        if (this.isAFK(player)) {
            this.reset(player);
            event.setCancelled(true);
        }
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
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED &&
                event.getAction() != EntityPotionEffectEvent.Action.CHANGED) return;

        if (this.isAFK(player)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && this.isAFK(player)) {
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