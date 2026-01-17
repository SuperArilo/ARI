package com.tty.listener.player;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.commands.maintenance;
import com.tty.dto.SpawnLocation;
import com.tty.dto.event.OnZakoSavedEvent;
import com.tty.dto.state.player.PlayerSaveState;
import com.tty.entity.BanPlayer;
import com.tty.entity.ServerPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.enumType.FilePath;
import com.tty.function.BanPlayerManager;
import com.tty.function.PlayerManager;
import com.tty.lib.Lib;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.Teleporting;
import com.tty.function.WhitelistManager;
import com.tty.lib.Log;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.states.PlayerSaveStateService;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static com.tty.commands.sub.EnderChestToPlayer.OFFLINE_ON_EDIT_ENDER_CHEST_LIST;


public class OnPlayerJoinAndLeaveListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void banCheck(AsyncPlayerPreLoginEvent event) {
        EntityRepository<Object, BanPlayer> banPlayerEntityRepository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        UUID uuid = event.getUniqueId();
        BanPlayer banPlayer;
        try {
            banPlayer = banPlayerEntityRepository.get(new BanPlayerManager.QueryKey(uuid.toString())).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error(e, "query ban list error on uuid {}", uuid.toString());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(e.getMessage()));
            return;
        }
        if (banPlayer == null) return;
        if (banPlayer.getEndTime() <= System.currentTimeMillis()) {
            banPlayerEntityRepository.delete(banPlayer);
            Log.debug("free player uuid {}.", banPlayer.getPlayerUUID());
        } else {
            List<String> value = Ari.C_INSTANCE.getValue("server.player.baned", FilePath.LANG, new TypeToken<List<String>>() {
            }.getType(), List.of());

            Component component = ComponentUtils.textList(value, Map.of(
                    LangType.BAN_REASON.getType(), ComponentUtils.text(banPlayer.getReason()),
                    LangType.BAN_END_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(banPlayer.getEndTime() - System.currentTimeMillis()))
            ));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, component);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void maintenance(AsyncPlayerPreLoginEvent event) {
        if (maintenance.MAINTENANCE_MODE){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ConfigUtils.t("server.maintenance.when-player-join"));
        }
        if (OFFLINE_ON_EDIT_ENDER_CHEST_LIST.contains(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void whitelist(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        if(!Ari.instance.getConfig().getBoolean("server.whitelist.enable", false)) return;
        EntityRepository<Object, ServerPlayer> playerEntityRepository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);
        EntityRepository<Object, WhitelistInstance> whitelistInstanceEntityRepository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        WhitelistInstance instance;
        try {
            instance = whitelistInstanceEntityRepository.get(new WhitelistManager.QueryKey(uuid.toString())).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error(e, "check whitelist on uuid {} error.", uuid.toString());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ComponentUtils.text(e.getMessage()));
            return;
        }
        if (instance == null) {
            if(Bukkit.getServer().getOperators().stream().anyMatch(op -> op.getUniqueId().equals(uuid))) {
                WhitelistInstance n = new WhitelistInstance();
                n.setAddTime(System.currentTimeMillis());
                n.setPlayerUUID(uuid.toString());
                n.setOperator(Operator.CONSOLE.getUuid());
                whitelistInstanceEntityRepository.create(n)
                    .exceptionally(i -> {
                        Log.error(i, "player uuid {} login error.", uuid.toString());
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(i.getMessage()));
                        return null;
                    });
            } else {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ConfigUtils.t("server.message.on-whitelist-login"));
                return;
            }
        }

        //判断玩家是否更改过名字
        ServerPlayer serverPlayer;
        try {
            serverPlayer = playerEntityRepository.get(new PlayerManager.QueryKey(uuid.toString())).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (serverPlayer == null) return;
        if (serverPlayer.getPlayerName().equals(event.getName())) return;
        Log.debug("layer changed name. old: {}, new: {}", serverPlayer.getPlayerName(), event.getName());
        serverPlayer.setPlayerName(event.getName());
        playerEntityRepository.update(serverPlayer);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        EntityRepository<Object, ServerPlayer> playerEntityRepository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);
        boolean first = Ari.instance.getConfig().getBoolean("server.message.on-first-login", false);
        boolean login = Ari.instance.getConfig().getBoolean("server.message.on-login", false);

        if (first || login) {
            event.joinMessage(null);
        }
        long nowLoginTime = System.currentTimeMillis();
        playerEntityRepository.get(new PlayerManager.QueryKey(player.getUniqueId().toString()))
            .thenCompose(i -> {
                if(i == null) {
                    ServerPlayer serverPlayer = new ServerPlayer();
                    serverPlayer.setPlayerName(player.getName());
                    serverPlayer.setPlayerUUID(player.getUniqueId().toString());
                    serverPlayer.setFirstLoginTime(System.currentTimeMillis());
                    playerEntityRepository.create(serverPlayer);
                } else {
                    i.setLastLoginOffTime(nowLoginTime);
                    playerEntityRepository.update(i);
                }
                return CompletableFuture.completedFuture(null);
            })
            .whenComplete((i, ex) -> {
                if (ex != null) {
                    Log.error("player {} login in server error.", player.getName());
                    player.kick(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-error")));
                    return;
                }
                Ari.STATE_MACHINE_MANAGER
                        .get(PlayerSaveStateService.class)
                        .addState(new PlayerSaveState(player, nowLoginTime));
                if(!player.hasPlayedBefore()) {
                    if (Ari.C_INSTANCE.getValue("main.first-join", FilePath.SPAWN_CONFIG, Boolean.class, false) &&
                            Ari.C_INSTANCE.getValue("main.enable", FilePath.SPAWN_CONFIG, Boolean.class, false)) {
                        SpawnLocation value = Ari.C_INSTANCE.getValue("main.location", FilePath.SPAWN_CONFIG, SpawnLocation.class, null);
                        if (value != null) {
                            Teleporting.create(
                                    Ari.instance,
                                    player,
                                new Location(
                                    Bukkit.getWorld(value.getWorldName()),
                                    value.getX(),
                                    value.getY(),
                                    value.getZ(),
                                    value.getYaw(),
                                    value.getPitch()
                                )
                            ).teleport();
                        } else {
                            Log.info("server not set spawn location.");
                        }
                    }
                    if(first) {
                        Ari.PLACEHOLDER.render("server.message.on-first-login", player).thenAccept(t -> Lib.Scheduler.run(Ari.instance, task -> Bukkit.broadcast(t)));
                        return;
                    }
                }
                if(login) {
                    Ari.PLACEHOLDER.render("server.message.on-login", player).thenAccept(t -> Lib.Scheduler.run(Ari.instance, task -> Bukkit.broadcast(t)));
                }
            });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(Ari.instance.getConfig().getBoolean("server.message.on-leave")) {
            event.quitMessage(null);
            Ari.PLACEHOLDER.render("server.message.on-leave", player).thenAccept(i -> Lib.Scheduler.run(Ari.instance, t -> Bukkit.broadcast(i)));
        }
        List<PlayerSaveState> states = Ari.STATE_MACHINE_MANAGER
                .get(PlayerSaveStateService.class)
                .getStates(player);
        if (!states.isEmpty()) {
            states.getFirst().setCount(Integer.MAX_VALUE);
        }
    }

    @EventHandler
    public void onSave(OnZakoSavedEvent event) {
        Player player = event.getPlayer();
        PlayerSaveStateService service = Ari.STATE_MACHINE_MANAGER.get(PlayerSaveStateService.class);
        if (!service.isNotHaveState(player)) return;

        Ari.STATE_MACHINE_MANAGER
                .get(PlayerSaveStateService.class)
                .addState(new PlayerSaveState(player, System.currentTimeMillis()));
    }

}
