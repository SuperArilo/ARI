package com.tty.tool;

import com.tty.Ari;
import com.tty.commands.args.zako.ZakoInfoArgs;
import com.tty.dto.SleepingWorld;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.dto.state.teleport.RandomTpState;
import com.tty.entity.ServerPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.enumType.FilePath;
import com.tty.enumType.lang.*;
import com.tty.function.PlayerManager;
import com.tty.function.WhitelistManager;
import com.tty.lib.dto.state.State;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.services.impl.PlaceholderRegistryImpl;
import com.tty.lib.services.placeholder.*;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.listener.player.PlayerSkipNight;
import com.tty.states.teleport.PreTeleportStateService;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.states.teleport.TeleportStateService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static com.tty.listener.teleport.RecordLastLocationListener.TELEPORT_LAST_LOCATION;

public class Placeholder extends BasePlaceholder<FilePath> {

    public Placeholder() {
        super(Ari.C_INSTANCE, FilePath.LANG);
        this.init();
    }

    public void init() {
        PlaceholderRegistryImpl registry = new PlaceholderRegistryImpl();
        this.register(registry);
        this.addRegister(registry);
    }

    private void register(PlaceholderRegistry registry) {
        registry.register(PlaceholderDefinition.of(
                LangTpa.TPA_SENDER,
                PlaceholderResolve.ofPlayer(player -> this.set(player.getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                LangTpa.TPA_BE_SENDER,
                PlaceholderResolve.of(player -> {
                    List<PreEntityToEntityState> states = Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    PreEntityToEntityState first = states.getFirst();
                    return this.set(first.getTarget().getName());
                }, offlinePlayer -> this.empty())
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.DEATH_LOCATION,
                PlaceholderResolve.ofPlayer(player -> {
                    Location deathLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
                    if (deathLocation == null) return this.empty();
                    return this.set(FormatUtils.XYZText(deathLocation.getX(), deathLocation.getY(), deathLocation.getZ()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                LangTime.SLEEP_PLAYERS,
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
                LangTime.SKIP_NIGHT_TICK_INCREMENT,
                PlaceholderResolve.ofPlayer(player -> {
                    World world = player.getWorld();
                    SleepingWorld sleepingWorld = PlayerSkipNight.SLEEPING_WORLD.get(world);
                    return this.set(String.valueOf(sleepingWorld.getTimeManager().getAddTick()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                LangRTP.RTP_SEARCH_COUNT,
                PlaceholderResolve.ofPlayer(player -> {
                    List<RandomTpState> states = Ari.STATE_MACHINE_MANAGER.get(RandomTpStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    RandomTpState first = states.getFirst();
                    return this.set(String.valueOf(first.getMax_count() - first.getCount()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                LangTeleport.TELEPORT_DELAY,
                PlaceholderResolve.ofPlayer(player -> {
                    List<State> states = Ari.STATE_MACHINE_MANAGER.get(TeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    State first = states.getFirst();
                    return this.set(String.valueOf(first.getMax_count() - first.getCount()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.PLAYER_NAME,
                PlaceholderResolve.of(
                        player -> this.set(player.getName()),
                        offlinePlayer -> {
                            String name = offlinePlayer.getName();
                            return this.set(name == null ? "null":name);
                        })
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.FIRST_LOGIN_SERVER_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getFirstLoginTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.LAST_LOGIN_SERVER_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getLastLoginOffTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.TOTAL_TIME_ON_SERVER,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getTotalOnlineTime()))))
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.PLAYER_WORLD,
                PlaceholderResolve.ofPlayer(player -> this.set(player.getWorld().getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.PLAYER_LOCATION,
                PlaceholderResolve.ofPlayer(player -> this.set(FormatUtils.XYZText(player.getX(), player.getY(), player.getZ())))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.ZAKO_WHITELIST_OPERATOR,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new WhitelistManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(whitelistInstance -> {
                            String operator;
                            if(whitelistInstance.getOperator().equals(Operator.CONSOLE.getUuid())) {
                                operator = "CONSOLE";
                            } else {
                                operator = Bukkit.getOfflinePlayer(UUID.fromString(whitelistInstance.getOperator())).getName();
                            }
                            return ComponentUtils.text(operator == null ? "null":operator);
                        }))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.ZAKO_WHITELIST_ADD_TIME,
                PlaceholderResolve.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new WhitelistManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getAddTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
    }

}
