package com.tty.ari.listener.player;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.api.event.PlayerEnterAFKEvent;
import com.tty.api.event.PlayerLeaveAFKEvent;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.states.PlayerAFKService;
import com.tty.ari.states.PlayerVanishService;
import com.tty.ari.tool.ConfigUtils;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerAFKStatusListener implements Listener {

    @EventHandler
    public void onEnter(PlayerEnterAFKEvent event) {
        Player player = event.getPlayer();

        if (!Ari.instance.getStatusManager().get(PlayerVanishService.class).isNotHaveState(player)) {
            event.setCancelled(true);
            return;
        }

        CompletableFuture<Component> breakHintFuture = ConfigUtils.t("server.player.afk.break-hint");
        CompletableFuture<Component> titleFuture = ConfigUtils.t("server.player.afk.title", player);
        CompletableFuture<List<Component>> tListFuture = ConfigUtils.tAsList("server.player.afk.sub-title", player);
        CompletableFuture<Component> message = ConfigUtils.t("server.player.afk.player-leave", player);

        CompletableFuture.allOf(breakHintFuture, titleFuture, tListFuture, message).thenRunAsync(() -> {
            List<Component> list = tListFuture.join();
            Ari.instance.getScheduler().runAtEntity(player, i -> {
                player.showTitle(Ari.instance.getComponentTool().setPlayerTitle(
                        titleFuture.join(),
                        list.get(PublicFunctionUtils.randomGenerator(0, list.size() - 1)).append(breakHintFuture.join()),
                        Duration.ofMillis(500),
                        Duration.ofMillis(Integer.MAX_VALUE),
                        Duration.ofMillis(500))
                );
                Bukkit.getServer().broadcast(message.join());
            }, null);
        }, Ari.instance.getExecutorAsync());
    }

    @EventHandler
    public void onLeave(PlayerLeaveAFKEvent event) {
        Player player = event.getPlayer();
        ConfigUtils.t("server.player.afk.player-back", player).thenAccept(msg -> {
            if (Ari.instance.getStatusManager().get(PlayerVanishService.class).isNotHaveState(player)) {
                Bukkit.getServer().broadcast(msg);
            }
            player.clearTitle();
        });
    }

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