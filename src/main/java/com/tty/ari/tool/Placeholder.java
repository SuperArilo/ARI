package com.tty.ari.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.PartitionKey;
import com.tty.api.service.impl.PlaceholderRegistryImpl;
import com.tty.api.service.placeholder.PlaceholderDefinition;
import com.tty.api.service.placeholder.PlaceholderRegistry;
import com.tty.api.service.placeholder.PlaceholderResolve;
import com.tty.api.state.AsyncState;
import com.tty.api.state.State;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.TimeFormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.dto.SleepingWorld;
import com.tty.ari.dto.state.teleport.PreEntityToEntityState;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.ari.entity.BanPlayer;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.enumType.lang.*;
import com.tty.ari.listener.player.PlayerSkipNight;
import com.tty.ari.states.PlayerChatService;
import com.tty.ari.states.teleport.PreTeleportStateService;
import com.tty.ari.states.teleport.RandomTpStateService;
import com.tty.ari.states.teleport.TeleportStateService;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.tty.ari.listener.teleport.RecordLastLocationListener.TELEPORT_LAST_LOCATION;

public class Placeholder {

    private static final Properties PLUGIN_INFO = new Properties();

    static {
        try (InputStream inputStream = Ari.instance.getClass().getClassLoader().getResourceAsStream("git.properties")) {
            if (inputStream == null) {
                Ari.instance.getLog().debug("could not found file git.properties in jar.");
            } else {
                PLUGIN_INFO.load(inputStream);
            }
        } catch (IOException e) {
            Ari.instance.getLog().debug(e, "could not found file git.properties in jar.");
        }
    }

    public static PlaceholderRegistry register() {

        PlaceholderRegistry registry = new PlaceholderRegistryImpl();

        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.SERVER_VERSION,
                PlaceholderResolve.ofWhenNullSync((() -> Bukkit.getName() + " " + Bukkit.getServer().getVersion()))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_NAME,
                PlaceholderResolve.ofWhenNullSync((() -> Ari.instance.getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_BRANCH,
                PlaceholderResolve.ofWhenNullSync((() -> PLUGIN_INFO.getProperty("git.branch")))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_BUILD_TIME,
                PlaceholderResolve.ofWhenNullSync((() -> OffsetDateTime.parse(PLUGIN_INFO.getProperty("git.build.time")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_BUILD_VERSION,
                PlaceholderResolve.ofWhenNullSync((() -> PLUGIN_INFO.getProperty("git.build.version") + "-" + PLUGIN_INFO.getProperty("git.commit.id.abbrev")))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_COMMIT_ID_ABBREV,
                PlaceholderResolve.ofWhenNullSync((() -> PLUGIN_INFO.getProperty("git.commit.id.abbrev")))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_COMMIT_MESSAGE,
                PlaceholderResolve.ofWhenNullSync((() -> PLUGIN_INFO.getProperty("git.commit.message.full")))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_COMMIT_TIME,
                PlaceholderResolve.ofWhenNullSync((() -> OffsetDateTime.parse(PLUGIN_INFO.getProperty("git.commit.time")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_COMMIT_USER_NAME,
                PlaceholderResolve.ofWhenNullSync((() -> PLUGIN_INFO.getProperty("git.commit.user.name")))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_GIT_TAG,
                PlaceholderResolve.ofWhenNullSync((() -> PLUGIN_INFO.getProperty("git.tag")))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.PLUGIN_DEBUG_STATUS,
                PlaceholderResolve.ofWhenNullSync(()-> String.valueOf(Ari.instance.isDebug())))
        );
        registry.register(PlaceholderDefinition.of(
                PlaceholderTpa.TPA_SENDER,
                PlaceholderResolve.ofPlayerSync(player -> {
                    List<PreEntityToEntityState> states = Ari.instance.getStatusManager().get(PreTeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return "";
                    PreEntityToEntityState first = states.getFirst();
                    return first.getOwner().getName();
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTpa.TPA_BE_SENDER,
                PlaceholderResolve.ofSync(player -> {
                    List<PreEntityToEntityState> states = Ari.instance.getStatusManager().get(PreTeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return "";
                    PreEntityToEntityState first = states.getFirst();
                    return first.getTarget().getName();
                }, offlinePlayer -> "")
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.DEATH_LOCATION,
                PlaceholderResolve.ofPlayerSync(player -> {
                    Location deathLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
                    if (deathLocation == null) return "";
                    return FormatUtils.XYZText(deathLocation.getX(), deathLocation.getY(), deathLocation.getZ());
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTime.SLEEP_PLAYERS,
                PlaceholderResolve.ofPlayerSync(player -> {
                    int sleepingCount = 0;
                    World world = player.getWorld();
                    for (Player p : world.getPlayers()) {
                        if (p.isDeeplySleeping()) {
                            sleepingCount++;
                        }
                    }
                    return String.valueOf(sleepingCount);
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTime.SKIP_NIGHT_TICK_INCREMENT,
                PlaceholderResolve.ofPlayerSync(player -> {
                    World world = player.getWorld();
                    SleepingWorld sleepingWorld = PlayerSkipNight.SLEEPING_WORLD.get(world);
                    return String.valueOf(sleepingWorld.getTimeManager().getAddTick());
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderRTP.RTP_SEARCH_COUNT,
                PlaceholderResolve.ofPlayerSync(player -> {
                    List<RandomTpState> states = Ari.instance.getStatusManager().get(RandomTpStateService.class).getStates(player);
                    if (states.isEmpty()) return "";
                    RandomTpState first = states.getFirst();
                    return String.valueOf(first.getMax_count() - first.getCount().get());
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTeleport.TELEPORT_DELAY,
                PlaceholderResolve.ofPlayerSync(player -> {
                    List<AsyncState> states = Ari.instance.getStatusManager().get(TeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return "";
                    State first = states.getFirst();
                    return String.valueOf(first.getMax_count() - first.getCount().get());
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_NAME,
                PlaceholderResolve.ofSync(
                        Player::getName,
                        offlinePlayer -> {
                            String name = offlinePlayer.getName();
                            return name == null ? offlinePlayer.getUniqueId().toString():name;
                        })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.FIRST_LOGIN_SERVER_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            return TimeFormatUtils.format(i.getFirstLoginTime(), "yyyy-MM-dd HH:mm:ss");
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.LAST_LOGIN_SERVER_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            return TimeFormatUtils.format(i.getLastLoginOffTime(), "yyyy-MM-dd HH:mm:ss");
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.TOTAL_TIME_ON_SERVER,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            return TimeFormatUtils.format(i.getTotalOnlineTime());
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_WORLD,
                PlaceholderResolve.ofPlayerSync(player -> player.getWorld().getName())
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_LOCATION,
                PlaceholderResolve.ofPlayerSync(player -> FormatUtils.XYZText(player.getX(), player.getY(), player.getZ()))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.ZAKO_WHITELIST_OPERATOR,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(whitelistInstance -> {
                            if (whitelistInstance == null) return Ari.DATA_SERVICE.getValue("base.none");
                            String name;
                            Operator operator = Operator.fromUuid(whitelistInstance.getOperator());
                            if (operator == null) {
                                name = PlayerCache.getPlayer(UUID.fromString(whitelistInstance.getOperator())).getName();
                            } else {
                                name = operator.name();
                            }
                            return operator == null ? Ari.DATA_SERVICE.getValue("base.none"):name;
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.ZAKO_WHITELIST_ADD_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            return TimeFormatUtils.format(i.getAddTime(), "yyyy-MM-dd HH:mm:ss");
                        })
        )));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoList.ZAKO_LIST_ITEM_NAME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> {
                    String name = offlinePlayer.getName();
                    return CompletableFuture.completedFuture((name == null ? offlinePlayer.getUniqueId().toString():name));
                })));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoList.ZAKO_LIST_ITEM_REMARK,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            String remark = i.getRemark();
                            if (remark == null || remark.isEmpty()) {
                                return Ari.DATA_SERVICE.getValue("base.none");
                            }
                            return remark;
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderMaintenance.MAINTENANCE_KICK_DEALY,
                PlaceholderResolve.ofPlayerSync(player -> String.valueOf(Ari.instance.getConfig().getInt("server.maintenance.kick_delay", 10)))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_T0TAL_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                .thenApply(i -> {
                    if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                    return TimeFormatUtils.format(i.getEndTime() - i.getStartTime());
                }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_END_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                .thenApply(i -> {
                    if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                    return TimeFormatUtils.format(Math.max(0, i.getEndTime() - System.currentTimeMillis()));
                }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_REASON,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                        .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            return i.getReason();
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_OPERATOR,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                        .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.DATA_SERVICE.getValue("base.none");
                            String name;
                            Operator operator = Operator.fromUuid(i.getOperator());
                            if(operator != null) {
                                name = operator.name();
                            } else {
                                name = PlayerCache.getPlayer(UUID.fromString(i.getOperator())).getName();
                            }
                            return name;
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_NAME_PREFIX,
                PlaceholderResolve.ofOfflinePlayerSync(offlinePlayer -> {
                    String uuid = offlinePlayer.getUniqueId().toString();
                    ServerPlayer serverPlayer = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class).getDirectFromCache(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid), PartitionKey.global());
                    if (serverPlayer == null) return "";
                    return serverPlayer.getNamePrefix();
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_NAME_SUFFIX,
                PlaceholderResolve.ofOfflinePlayerSync(offlinePlayer -> {
                    String uuid = offlinePlayer.getUniqueId().toString();
                    ServerPlayer serverPlayer = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class).getDirectFromCache(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid), PartitionKey.global());
                    if (serverPlayer == null) return "";
                    return serverPlayer.getNameSuffix();
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayerChat.SOURCE_DISPLAY_NAME,
                PlaceholderResolve.ofPlayerSync(Player::getName)
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayerChat.CHAT_MESSAGE,
                PlaceholderResolve.ofPlayerSync(player -> MiniMessage.miniMessage().serialize(Ari.instance.getStatusManager().get(PlayerChatService.class).getStates(player).getFirst().getMessage()))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_GAME_MODE,
                PlaceholderResolve.ofPlayerSync(player -> {
                    Component translatable = Component.translatable(player.getGameMode().translationKey());
                    Component resolved;
                    try {
                        resolved = PaperComponents.resolveWithContext(translatable, player, player);
                    } catch (IOException e) {
                        resolved = Component.empty();
                    }
                    return PlainTextComponentSerializer.plainText().serialize(resolved);
                })
        ));

        return registry;
    }

}
