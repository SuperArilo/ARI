package com.tty.ari.states;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.event.CustomPlayerRespawnEvent;
import com.tty.ari.dto.state.player.PlayerMorphState;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collections;
import java.util.List;

public class PlayerMorphService extends StateService<PlayerMorphState> implements Listener {

    public PlayerMorphService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
        Bukkit.getPluginManager().registerEvents(this, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerMorphState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) {
            state.setOver(true);
            return;
        }
        state.setRunning(true);
        Ari.instance.getScheduler().runAtEntity(state.getOwner(), i -> {
            if (!player.isOnline()) {
                state.setOver(true);
            }
            state.setRunning(false);
        }, null);
    }

    @Override
    protected void abortAddState(PlayerMorphState state) {
    }

    @Override
    protected void passAddState(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.morphPlayer(player, state.getType());
        ConfigUtils.t("function.morph.changed", player).thenAccept(player::sendMessage);
    }

    @Override
    protected void onEarlyExit(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.restorePlayer(player);
    }

    @Override
    protected void onFinished(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.restorePlayer(player);
    }

    @Override
    protected void onServiceAbort(PlayerMorphState state) {
        state.setOver(true);
    }

    @Override
    public void onReload() {

    }

    @EventHandler
    public void onPlayerRespawnPaper(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<PlayerMorphState> states = this.getStates(player);
        if (states.isEmpty()) return;
        Ari.instance.getScheduler().runAtEntity(player, i -> this.morphPlayer(player, states.getFirst().getType()), null);
    }

    @EventHandler
    public void onPlayerRespawnFolia(CustomPlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<PlayerMorphState> states = this.getStates(player);
        if (states.isEmpty()) return;
        Ari.instance.getScheduler().runAtEntity(player, i -> this.morphPlayer(player, states.getFirst().getType()), null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        if (!newPlayer.isOnline()) return;
        for (PlayerMorphState state : this.getAllStates()) {
            if (state.getOwner() instanceof Player player && player.isOnline()) {
                Ari.instance.getScheduler().runAtEntityLater(newPlayer, i -> this.morphPlayerToViewer(player, state.getType(), newPlayer), null, 20L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.stopStateByOwner(event.getPlayer());
    }

    private void morphPlayer(Player player, EntityType type) {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            this.morphPlayerToViewer(player, type, onlinePlayer);
        }
    }

    private void morphPlayerToViewer(Player target, EntityType type, Player viewer) {
        if (target.equals(viewer)) return;

        PacketContainer destroy = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntLists().write(0, Collections.singletonList(target.getEntityId()));
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, destroy);

        PacketContainer spawn = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawn.getIntegers().write(0, target.getEntityId());
        spawn.getUUIDs().write(0, target.getUniqueId());
        spawn.getEntityTypeModifier().write(0, type);
        spawn.getDoubles().write(0, target.getLocation().getX());
        spawn.getDoubles().write(1, target.getLocation().getY());
        spawn.getDoubles().write(2, target.getLocation().getZ());
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, spawn);
    }

    private void restorePlayer(Player player) {
        if (!player.isOnline()) return;
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                PacketContainer destroy = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                destroy.getIntLists().write(0, Collections.singletonList(player.getEntityId()));
                Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, destroy);
                Ari.instance.getScheduler().runAtEntity(viewer, i -> {
                    viewer.hidePlayer(Ari.instance, player);
                    viewer.showPlayer(Ari.instance, player);
                }, null);
            }
        }
    }
}