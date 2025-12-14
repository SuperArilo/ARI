package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.state.player.PlayerSaveState;
import com.tty.entity.sql.ServerPlayer;
import com.tty.entity.sql.WhitelistInstance;
import com.tty.enumType.FilePath;
import com.tty.function.PlayerManager;
import com.tty.function.Teleporting;
import com.tty.function.WhitelistManager;
import com.tty.lib.Log;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.states.PlayerSaveStateService;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class OnPlayerJoinAndLeaveListener implements Listener {


    @EventHandler
    public void whitelist(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if(!Ari.instance.getConfig().getBoolean("server.whitelist.enable", false)) return;
        WhitelistManager manager = new WhitelistManager(false);
        PlayerManager playerManager = new PlayerManager(true);
        manager.getInstance(player.getUniqueId().toString())
                .thenCompose(instance -> {
                    if (instance == null) {
                        if(player.isOp()) {
                            WhitelistInstance n = new WhitelistInstance();
                            n.setAddTime(System.currentTimeMillis());
                            n.setPlayerUUID(player.getUniqueId().toString());
                            manager.createInstance(n);
                        } else {
                            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ConfigUtils.t("server.message.on-whitelist-login"));
                        }
                        return CompletableFuture.completedFuture(null);
                    } else {
                        return playerManager.getInstance(instance.getPlayerUUID());
                    }
                })
                .thenAccept(instance -> {
                    if (instance == null) return;
                    if (instance.getPlayerName().equals(player.getName())) return;
                    Log.debug("layer changed name. old: %s, new: %s", instance.getPlayerName(), player.getName());
                    instance.setPlayerName(player.getName());
                    playerManager.modify(instance);
                }).exceptionally(i -> {
                    Log.error(i, "whitelist error");
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ComponentUtils.text(i.getMessage()));
                    return null;
                });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        boolean first = Ari.instance.getConfig().getBoolean("server.message.on-first-login", false);
        boolean login = Ari.instance.getConfig().getBoolean("server.message.on-login", false);

        PlayerManager manager = new PlayerManager(true);
        if (first || login) {
            event.joinMessage(null);
        }
        long nowLoginTime = System.currentTimeMillis();
        manager.getInstance(player.getUniqueId().toString())
                .thenAccept(i -> {
                    if(!player.hasPlayedBefore()) {
                        if (Ari.C_INSTANCE.getValue("main.first-join", FilePath.SPAWN_CONFIG, Boolean.class, false) &&
                                Ari.C_INSTANCE.getValue("main.enable", FilePath.SPAWN_CONFIG, Boolean.class, false)) {
                            Location value = Ari.C_INSTANCE.getValue("main.location", FilePath.SPAWN_CONFIG, Location.class);
                            if (value != null) {
                                Teleporting.create(player, value).teleport();
                            }
                        }
                        if(first) {
                            Bukkit.broadcast(ConfigUtils.t("server.message.on-first-login", Map.of(LangType.PLAYER_NAME.getType(), Component.text(player.getName()))));
                        }
                    }
                    if(i == null) {
                        ServerPlayer serverPlayer = new ServerPlayer();
                        serverPlayer.setPlayerName(player.getName());
                        serverPlayer.setPlayerUUID(player.getUniqueId().toString());
                        serverPlayer.setFirstLoginTime(System.currentTimeMillis());
                        manager.createInstance(serverPlayer);
                    } else {
                        i.setLastLoginOffTime(nowLoginTime);
                        manager.modify(i);
                    }
                    if(login) {
                        Bukkit.broadcast(ConfigUtils.t("server.message.on-login", Map.of(LangType.PLAYER_NAME.getType(), Component.text(player.getName()))));
                    }

                    Ari.instance.stateMachineManager
                            .get(PlayerSaveStateService.class)
                            .addState(new PlayerSaveState(player, nowLoginTime));
                }).exceptionally(i -> {
                   Log.error(i, "get player data error");
                   return null;
                });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(Ari.instance.getConfig().getBoolean("server.message.on-leave")) {
            event.quitMessage(ConfigUtils.t("server.message.on-leave", Map.of(LangType.PLAYER_NAME.getType(), Component.text(player.getName()))));
        }
        List<PlayerSaveState> states = Ari.instance.stateMachineManager
                .get(PlayerSaveStateService.class)
                .getStates(player);
        if (!states.isEmpty()) {
            states.getFirst().setCount(Integer.MAX_VALUE);
        }
    }

}
