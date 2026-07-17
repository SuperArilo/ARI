package com.tty.ari.listener.player;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.StatusManager;
import com.tty.api.enumType.Operator;
import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.player.MaintenanceBossBarState;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.dto.state.player.PlayerSaveState;
import com.tty.ari.entity.BanPlayer;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.MaintenanceBossBarService;
import com.tty.ari.states.PlayerAFKService;
import com.tty.ari.states.PlayerSaveDataStateService;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OnPlayerJoinAndLeaveListener implements Listener {

    private boolean whitelistEnable;

    private boolean messageFirstJoin;
    private boolean messageOnLogin;
    private boolean messageOnLeave;

    @EventHandler(priority = EventPriority.LOWEST)
    public void banCheck(AsyncPlayerPreLoginEvent event) {
        EntityRepository<BanPlayer> repository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        UUID uuid = event.getUniqueId();
        BanPlayer banPlayer;
        LambdaQueryWrapper<BanPlayer> wrapper = new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, uuid.toString());
        try {
            banPlayer = repository.get(wrapper, PartitionKey.global()).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Ari.instance.getLog().error(e, "query ban list error on uuid {}", uuid.toString());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error")));
            return;
        }
        if (banPlayer == null) return;
        if (banPlayer.getEndTime() <= System.currentTimeMillis()) {
            repository.delete(wrapper, PartitionKey.global());
            Ari.instance.getLog().debug("free player uuid {}.", banPlayer.getPlayerUUID());
        } else {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ConfigUtils.tList("server.player.banned", PlayerCache.getPlayer(uuid)).join());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void maintenance(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        UUID uuid = event.getUniqueId();
        MaintenanceBossBarService service = Ari.instance.getStatusManager().get(MaintenanceBossBarService.class);
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(uuid);
        if (service.isMaintenance() && !offlinePlayer.isOp()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ConfigUtils.t("server.maintenance.when-player-join").join());
            return;
        }
        if (Ari.instance.getStatusManager().get(GuiManagerStateService.class).getAllStates().stream().anyMatch(t -> (t instanceof OnCheckPlayerGuiState state && state.getMonitoree().getUniqueId().equals(offlinePlayer.getUniqueId())))) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
        }
    }

    @EventHandler
    public void whitelist(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        UUID uuid = event.getUniqueId();
        if(!this.whitelistEnable) return;

        EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        WhitelistInstance instance;
        try {
            instance = repository.get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString()), PartitionKey.global()).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            Ari.instance.getLog().error(e, "check whitelist on uuid {} error.", uuid.toString());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error")));
            return;
        }
        if (instance == null) {
            if(Bukkit.getServer().getOperators().stream().anyMatch(op -> op.getUniqueId().equals(uuid))) {
                WhitelistInstance n = new WhitelistInstance();
                n.setAddTime(System.currentTimeMillis());
                n.setPlayerUUID(uuid.toString());
                n.setOperator(Operator.CONSOLE.getUuid());
                try {
                    repository.create(n, PartitionKey.global()).join();
                } catch (Exception e) {
                    Ari.instance.getLog().error(e, "player uuid {} login error.", uuid.toString());
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error")));
                }
            } else {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ConfigUtils.t("server.message.on-whitelist-login").join());
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        String playerName = player.getName();

        EntityRepository<ServerPlayer> repository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);

        if (this.messageFirstJoin || this.getMessageOnLogin()) {
            event.joinMessage(null);
        }
        long nowLoginTime = System.currentTimeMillis();
        LambdaQueryWrapper<ServerPlayer> wrapper = new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, player.getUniqueId().toString());
        repository.get(wrapper, PartitionKey.global()).thenCompose(i -> {
            //玩家第一次登录服务器，创建资料
            if(i == null) {
                ServerPlayer serverPlayer = new ServerPlayer();
                serverPlayer.setPlayerName(playerName);
                serverPlayer.setPlayerUUID(player.getUniqueId().toString());
                serverPlayer.setFirstLoginTime(System.currentTimeMillis());
                return repository.create(serverPlayer, PartitionKey.global());
            } else {
                //玩家已经登录过服务器，更新上次登录服务器的时间，再判断是否更改过名称
                i.setLastLoginOffTime(nowLoginTime);
                if (!i.getPlayerName().equals(playerName)) {
                    i.setPlayerName(playerName);
                    Ari.instance.getLog().debug("player changed name. old: {}, new: {}", i.getPlayerName(), playerName);
                }
                try {
                    repository.update(i, wrapper, PartitionKey.global());
                } catch (Exception e) {
                    Ari.instance.getLog().warn(e, "update player {} profiles error", playerName);
                }
                return CompletableFuture.completedFuture(i);
            }
        }).whenComplete((i, ex) -> {
            if (ex != null) {
                Ari.instance.getLog().error(ex);
                Ari.instance.getScheduler().run(r -> player.kick(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error"))));
                return;
            }

            FunctionConfig config = Ari.instance.getConfigurationManager().get(FunctionConfig.class);
            if(!player.hasPlayedBefore() && (config.getSpawnFirstJoin() && config.isEnable(TeleportType.SPAWN))) {
                SpawnLocation spawnLocation = config.getSpawnLocation();
                if (config.getSpawnLocation() != null) {
                    Ari.TELEPORTING_SERVICE.teleport(player, player.getLocation(), new Location(
                            Bukkit.getServer().getWorld(spawnLocation.getWorldName()),
                            spawnLocation.getX(),
                            spawnLocation.getY(),
                            spawnLocation.getZ(),
                            spawnLocation.getYaw(),
                            spawnLocation.getPitch()
                    ));
                } else {
                    Ari.instance.getLog().info("server not set spawn location.");
                }
                if(this.messageFirstJoin) {
                    ConfigUtils.t("server.message.on-first-login", player).thenAccept(t -> Ari.instance.getScheduler().run(task -> Bukkit.broadcast(t)));
                    return;
                }
            }
            if(this.messageOnLogin) {
                ConfigUtils.t("server.message.on-login", player).thenAccept(t -> Ari.instance.getScheduler().run(task -> Bukkit.broadcast(t)));
            }
            //添加玩家登录的状态
            Ari.instance.getStatusManager().get(PlayerSaveDataStateService.class).addState(new PlayerSaveState(player, nowLoginTime));
            Ari.instance.getStatusManager().get(PlayerAFKService.class).addState(new PlayerAFKState(player));
            Ari.instance.getScheduler().runAtEntity(player, o -> {
                if(this.isPlayerInsideBlock(player)) {
                    Ari.instance.getLog().debug("player {} inside block, teleport safe location.", player.getName());
                    Location safeLocation = this.findSafeLocationAbove(player.getLocation());
                    Ari.TELEPORTING_SERVICE.teleport(player, player.getLocation(), safeLocation).after(() -> player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("function.teleport.not-safe-location"), player)));
                }
            }, () -> Ari.instance.getLog().error("error on player join server."));

            //如果处于维护模式，为 op 添加 bar
            MaintenanceBossBarService service = Ari.instance.getStatusManager().get(MaintenanceBossBarService.class);
            if (service.isMaintenance() && player.isOp()) {
                service.addState(new MaintenanceBossBarState(player));
            }

            //将玩家添加进缓存
            PlayerCache.addPlayer(player);

        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        StatusManager manager = Ari.instance.getStatusManager();
        if(this.messageOnLeave) {
            event.quitMessage(null);
            ConfigUtils.t("server.message.on-leave", player).thenAccept(i -> Ari.instance.getScheduler().run(t -> Bukkit.broadcast(i)));
        }

        for (PlayerSaveState state : manager.get(PlayerSaveDataStateService.class).getStates(player)) {
            state.setOver(true);
        }
        for (GuiState state : manager.get(GuiManagerStateService.class).getStates(player)) {
            state.setOver(true);
        }

        PlayerCache.removePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onReload(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        this.whitelistEnable = this.getWhitelistEnable();
        this.messageFirstJoin = this.getMessageFirstJoin();
        this.messageOnLogin = this.getMessageOnLogin();
        this.messageOnLeave = this.getMessageOnLeave();
    }

    private boolean isPlayerInsideBlock(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        double feetY = loc.getY();
        double headY = feetY + player.getHeight();

        Block feetBlock = loc.getBlock();
        if (!feetBlock.isPassable()) {
            double blockTop = feetBlock.getBoundingBox().getMaxY();
            if (feetY < blockTop - 0.1) {
                return true;
            }
        }
        Block headBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
        if (!headBlock.isPassable()) {
            double headBlockBottom = headBlock.getBoundingBox().getMinY();
            return headY > headBlockBottom + 0.1;
        }
        return false;
    }

    //当玩家的进入服务器后的位置嵌在方块里，则往上查到非固定方块为止
    private Location findSafeLocationAbove(Location start) {
        World world = start.getWorld();
        int x = start.getBlockX();
        int z = start.getBlockZ();
        int startY = Math.max(start.getBlockY(), 0);
        int maxY = world.getMaxHeight() - 2;

        for (int y = startY; y <= maxY; y++) {
            Block feetBlock = world.getBlockAt(x, y, z);
            Block headBlock = world.getBlockAt(x, y + 1, z);
            Block groundBlock = world.getBlockAt(x, y - 1, z);
            if (feetBlock.isPassable() && headBlock.isPassable() && !groundBlock.isPassable()) {
                double groundTop = groundBlock.getBoundingBox().getMaxY();
                return new Location(world, x + 0.5, groundTop, z + 0.5);
            }
        }
        return world.getSpawnLocation();
    }

    private boolean getWhitelistEnable() {
        return Ari.instance.getConfig().getBoolean("server.whitelist.enable", false);
    }

    private boolean getMessageFirstJoin() {
        return Ari.instance.getConfig().getBoolean("server.message.on-first-login", false);
    }

    private boolean getMessageOnLogin() {
        return Ari.instance.getConfig().getBoolean("server.message.on-login", false);
    }

    private boolean getMessageOnLeave() {
        return Ari.instance.getConfig().getBoolean("server.message.on-leave", false);
    }

}
