package com.tty.tool;

import com.tty.Ari;
import com.tty.commands.args.zako.ZakoInfoArgs;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.entity.ServerPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.enumType.FilePath;
import com.tty.enumType.lang.LangPlayer;
import com.tty.enumType.lang.LangTime;
import com.tty.enumType.lang.LangTpa;
import com.tty.enumType.lang.LangZakoInfo;
import com.tty.function.PlayerManager;
import com.tty.function.WhitelistManager;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.services.impl.PlaceholderRegistryImpl;
import com.tty.lib.services.placeholder.AsyncPlaceholder;
import com.tty.lib.services.placeholder.BasePlaceholder;
import com.tty.lib.services.placeholder.PlaceholderDefinition;
import com.tty.lib.services.placeholder.PlaceholderRegistry;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.states.teleport.PreTeleportStateService;
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
        this.setRegister(registry);
    }

    private void register(PlaceholderRegistry registry) {
        registry.register(PlaceholderDefinition.of(
            LangTpa.TPA_SENDER,
            AsyncPlaceholder.of(
                    player -> this.set(player.getName()),
                    offlinePlayer -> this.set(offlinePlayer.getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                LangTpa.TPA_BE_SENDER,
                AsyncPlaceholder.of(player -> {
                    List<PreEntityToEntityState> states = Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class).getStates(player);
                    if (states.isEmpty()) return this.empty();
                    PreEntityToEntityState first = states.getFirst();
                    return this.set(first.getTarget().getName());
                }, offlinePlayer -> this.empty())
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.DEATH_LOCATION,
                AsyncPlaceholder.ofPlayer(player -> {
                    Location deathLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
                    if (deathLocation == null) return this.empty();
                    return this.set(FormatUtils.XYZText(deathLocation.getX(), deathLocation.getY(), deathLocation.getZ()));
                })
        ));
        registry.register(PlaceholderDefinition.of(
                LangTime.SLEEP_PLAYERS,
                AsyncPlaceholder.ofPlayer(player -> {
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
                LangPlayer.PLAYER_NAME,
                AsyncPlaceholder.of(
                        player -> this.set(player.getName()),
                        offlinePlayer -> {
                            String name = offlinePlayer.getName();
                            return this.set(name == null ? "null":name);
                        })
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.FIRST_LOGIN_SERVER_TIME,
                AsyncPlaceholder.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getFirstLoginTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.LAST_LOGIN_SERVER_TIME,
                AsyncPlaceholder.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getLastLoginOffTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.TOTAL_TIME_ON_SERVER,
                AsyncPlaceholder.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getTotalOnlineTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.PLAYER_WORLD,
                AsyncPlaceholder.ofPlayer(player -> this.set(player.getWorld().getName()))
        ));
        registry.register(PlaceholderDefinition.of(
                LangPlayer.PLAYER_LOCATION,
                AsyncPlaceholder.ofPlayer(player -> this.set(FormatUtils.XYZText(player.getX(), player.getY(), player.getZ())))
        ));
        registry.register(PlaceholderDefinition.of(
                LangZakoInfo.ZAKO_WHITELIST_OPERATOR,
                AsyncPlaceholder.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
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
                AsyncPlaceholder.ofOfflinePlayer(offlinePlayer -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new WhitelistManager.QueryKey(offlinePlayer.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getAddTime(), ZakoInfoArgs.getPatternDatetime()))))
        ));
    }

}
