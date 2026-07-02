package com.tty.ari.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.AbstractJavaPlugin;
import com.tty.api.ServerPlatform;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.PartitionKey;
import com.tty.api.service.impl.PlaceholderRegistryImpl;
import com.tty.api.service.placeholder.BasePlaceholder;
import com.tty.api.service.placeholder.PlaceholderDefinition;
import com.tty.api.service.placeholder.PlaceholderRegistry;
import com.tty.api.service.placeholder.PlaceholderResolve;
import com.tty.api.state.State;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.TimeFormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.lang.LangConfig;
import com.tty.ari.dto.SleepingWorld;
import com.tty.ari.dto.state.teleport.PreEntityToEntityState;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.ari.entity.BanPlayer;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.enumType.lang.*;
import com.tty.ari.listener.player.PlayerSkipNight;
import com.tty.ari.states.teleport.PreTeleportStateService;
import com.tty.ari.states.teleport.RandomTpStateService;
import com.tty.ari.states.teleport.TeleportStateService;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;
import java.util.UUID;

import static com.tty.ari.listener.teleport.RecordLastLocationListener.TELEPORT_LAST_LOCATION;

@SuppressWarnings("deprecation")
public class Placeholder extends BasePlaceholder {

    public Placeholder(AbstractJavaPlugin plugin) {
        super(plugin, Ari.instance.getConfigurationManager().get(LangConfig.class));
        this.init();
    }

    public void init() {
        PlaceholderRegistryImpl registry = new PlaceholderRegistryImpl();
        this.register(registry);
        this.addRegister(registry);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void register(PlaceholderRegistry registry) {
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.SERVER_VERSION,
                PlaceholderResolve.ofWhenNull((() -> this.set(Bukkit.getName() + " " + Bukkit.getServer().getVersion())))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.ARI_VERSION,
                PlaceholderResolve.ofWhenNull(() -> {
                    String pluginInfo;
                    if (ServerPlatform.isFolia()) {
                        PluginMeta pluginMeta = Ari.instance.getPluginMeta();
                        pluginInfo = pluginMeta.getName() + " " + pluginMeta.getVersion();
                    } else {
                        PluginDescriptionFile description = Ari.instance.getDescription();
                        pluginInfo = description.getName() + " " + description.getVersion();
                    }
                    return this.set(pluginInfo);
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderServer.ARI_DEBUG_STATUS,
                PlaceholderResolve.ofWhenNull(()-> this.set(String.valueOf(Ari.instance.isDebug()))))
        );
        registry.register(PlaceholderDefinition.of(
                PlaceholderTpa.TPA_SENDER,
                PlaceholderResolve.ofPlayer(player -> {
                    List<PreEntityToEntityState> states = Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    PreEntityToEntityState first = states.getFirst();
                    return this.set(first.getOwner().getName());
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTpa.TPA_BE_SENDER,
                PlaceholderResolve.of(player -> {
                    List<PreEntityToEntityState> states = Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    PreEntityToEntityState first = states.getFirst();
                    return this.set(first.getTarget().getName());
                }, offlinePlayer -> this.empty())
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.DEATH_LOCATION,
                PlaceholderResolve.ofPlayer(player -> {
                    Location deathLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
                    if (deathLocation == null) return this.empty();
                    return this.set(FormatUtils.XYZText(deathLocation.getX(), deathLocation.getY(), deathLocation.getZ()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTime.SLEEP_PLAYERS,
                PlaceholderResolve.ofPlayer(player -> {
                    int sleepingCount = 0;
                    World world = player.getWorld();
                    for (Player p : world.getPlayers()) {
                        if (p.isDeeplySleeping()) {
                            sleepingCount++;
                        }
                    }
                    return this.set(String.valueOf(sleepingCount));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTime.SKIP_NIGHT_TICK_INCREMENT,
                PlaceholderResolve.ofPlayer(player -> {
                    World world = player.getWorld();
                    SleepingWorld sleepingWorld = PlayerSkipNight.SLEEPING_WORLD.get(world);
                    return this.set(String.valueOf(sleepingWorld.getTimeManager().getAddTick()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderRTP.RTP_SEARCH_COUNT,
                PlaceholderResolve.ofPlayer(player -> {
                    List<RandomTpState> states = Ari.STATE_MACHINE_MANAGER.get(RandomTpStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    RandomTpState first = states.getFirst();
                    return this.set(String.valueOf(first.getMax_count() - first.getCount().get()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderTeleport.TELEPORT_DELAY,
                PlaceholderResolve.ofPlayer(player -> {
                    List<State> states = Ari.STATE_MACHINE_MANAGER.get(TeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    State first = states.getFirst();
                    return this.set(String.valueOf(first.getMax_count() - first.getCount().get()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_NAME,
                PlaceholderResolve.of(
                        player -> this.set(player.getName()),
                        offlinePlayer -> {
                            String name = offlinePlayer.getName();
                            return this.set(name == null ? "null":name);
                        })
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.FIRST_LOGIN_SERVER_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            return Component.text(TimeFormatUtils.format(i.getFirstLoginTime(), "yyyy-MM-dd HH:mm:ss"));
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.LAST_LOGIN_SERVER_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            return Component.text(TimeFormatUtils.format(i.getLastLoginOffTime(), "yyyy-MM-dd HH:mm:ss"));
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.TOTAL_TIME_ON_SERVER,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            return Component.text(TimeFormatUtils.format(i.getTotalOnlineTime()));
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_WORLD,
                PlaceholderResolve.ofPlayer(player -> this.set(player.getWorld().getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderPlayer.PLAYER_LOCATION,
                PlaceholderResolve.ofPlayer(player -> this.set(FormatUtils.XYZText(player.getX(), player.getY(), player.getZ())))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.ZAKO_WHITELIST_OPERATOR,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(whitelistInstance -> {
                            if (whitelistInstance == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            String operator;
                            if(whitelistInstance.getOperator().equals(Operator.CONSOLE.getUuid())) {
                                operator = "CONSOLE";
                            } else {
                                operator = Bukkit.getOfflinePlayer(UUID.fromString(whitelistInstance.getOperator())).getName();
                            }
                            return Ari.instance.getComponentTool().text(operator == null ? Ari.DATA_SERVICE.getValue("base.none"):operator);
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoInfo.ZAKO_WHITELIST_ADD_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            return Component.text(TimeFormatUtils.format(i.getAddTime(), "yyyy-MM-dd HH:mm:ss"));
                        })
        )));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoList.ZAKO_LIST_ITEM_NAME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> {
                    String name = offlinePlayer.getName();
                    return this.set(name == null ? offlinePlayer.getUniqueId().toString():name);
                })));
        registry.register(PlaceholderDefinition.of(
                PlaceholderZakoList.ZAKO_LIST_ITEM_REMARK,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            String remark = i.getRemark();
                            if (remark == null || remark.isEmpty()) {
                                return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            }
                            return Ari.instance.getComponentTool().text(remark);
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderMaintenance.MAINTENANCE_KICK_DEALY,
                PlaceholderResolve.ofPlayer(player -> this.set(String.valueOf(Ari.instance.getConfig().getInt("server.maintenance.kick_delay", 10))))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_PLAYER_NAME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> this.set(offlinePlayer.getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_T0TAL_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                .thenApply(i -> {
                    if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                    return Component.text(TimeFormatUtils.format(i.getEndTime() - i.getStartTime()));
                }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_END_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                .thenApply(i -> {
                    if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                    return Component.text(TimeFormatUtils.format(i.getEndTime() - System.currentTimeMillis()));
                }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderBanPlayerType.BAN_REASON,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER.get(BanPlayer.class)
                        .get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, offlinePlayer.getUniqueId().toString()), PartitionKey.global())
                        .thenApply(i -> {
                            if (i == null) return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.none"));
                            return Ari.instance.getComponentTool().text(i.getReason());
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                PlaceholderShowItem.SHOW_ITEM,
                PlaceholderResolve.ofPlayer(player -> Ari.instance.getComponentTool().setHoverItemText(player.getInventory().getItemInMainHand()))
        ));
    }

}
