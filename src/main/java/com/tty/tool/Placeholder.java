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
import com.tty.lib.services.impl.PlaceholderDefinitionImpl;
import com.tty.lib.services.impl.PlaceholderRegistryImpl;
import com.tty.lib.services.placeholder.BasePlaceholder;
import com.tty.lib.services.placeholder.PlaceholderRegistry;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.states.teleport.PreTeleportStateService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
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
        registry.register(new PlaceholderDefinitionImpl<>(
                LangTpa.TPA_SENDER,
                ctx -> {
                    if (!(ctx instanceof Entity entity)) return this.empty();
                    return this.set(entity.getName());
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangTpa.TPA_BE_SENDER,
                ctx -> {
                    if (!(ctx instanceof Entity entity)) return this.empty();
                    List<PreEntityToEntityState> states = Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class).getStates(entity);
                    if (states.isEmpty()) return this.empty();
                    PreEntityToEntityState first = states.getFirst();
                    return this.set(first.getTarget().getName());
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangPlayer.DEATH_LOCATION,
                ctx -> {
                    if (!(ctx instanceof Player player)) return this.empty();
                    Location deathLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
                    if (deathLocation == null) return this.empty();
                    return this.set(FormatUtils.XYZText(deathLocation.getX(), deathLocation.getY(), deathLocation.getZ()));
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangTime.SLEEP_PLAYERS,
                ctx -> {
                    if (!(ctx instanceof Player player)) return this.empty();
                    int sleepingCount = 0;
                    World world = player.getWorld();
                    for (Player p : world.getPlayers()) {
                        if (p.isDeeplySleeping()) {
                            sleepingCount++;
                        }
                    }
                    return this.set(String.valueOf(sleepingCount));
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangPlayer.PLAYER_NAME,
                ctx -> {
                    if (ctx instanceof Player) {
                        return this.set(ctx.getName());
                    } else {
                        String name = ctx.getName();
                        return this.set(name == null ? "null" : name);
                    }
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangZakoInfo.FIRST_LOGIN_SERVER_TIME,
                ctx -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(ctx.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getFirstLoginTime(), ZakoInfoArgs.getPatternDatetime())))
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangZakoInfo.LAST_LOGIN_SERVER_TIME,
                ctx -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(ctx.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getLastLoginOffTime(), ZakoInfoArgs.getPatternDatetime())))
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangZakoInfo.TOTAL_TIME_ON_SERVER,
                ctx -> Ari.REPOSITORY_MANAGER
                        .get(ServerPlayer.class)
                        .get(new PlayerManager.QueryKey(ctx.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getTotalOnlineTime(), ZakoInfoArgs.getPatternDatetime())))
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangPlayer.PLAYER_WORLD,
                ctx -> {
                    if (ctx instanceof Player player) {
                        return this.set(player.getWorld().getName());
                    } else {
                        return this.set(Ari.DATA_SERVICE.getValue("base.no-record"));
                    }
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangPlayer.PLAYER_LOCATION,
                ctx -> {
                    if (ctx instanceof Player player) {
                        return this.set(FormatUtils.XYZText(player.getX(), player.getY(), player.getZ()));
                    } else {
                        return this.set(Ari.DATA_SERVICE.getValue("base.no-record"));
                    }
                }
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangZakoInfo.ZAKO_WHITELIST_OPERATOR,
                ctx -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new WhitelistManager.QueryKey(ctx.getUniqueId().toString()))
                        .thenApply(whitelistInstance -> {
                            String operator;
                            if(whitelistInstance.getOperator().equals(Operator.CONSOLE.getUuid())) {
                                operator = "CONSOLE";
                            } else {
                                operator = Bukkit.getOfflinePlayer(UUID.fromString(whitelistInstance.getOperator())).getName();
                            }
                            return ComponentUtils.text(operator == null ? "null":operator);
                        })
        ));
        registry.register(new PlaceholderDefinitionImpl<>(
                LangZakoInfo.ZAKO_WHITELIST_ADD_TIME,
                ctx -> Ari.REPOSITORY_MANAGER
                        .get(WhitelistInstance.class)
                        .get(new WhitelistManager.QueryKey(ctx.getUniqueId().toString()))
                        .thenApply(i -> Component.text(TimeFormatUtils.format(i.getAddTime(), ZakoInfoArgs.getPatternDatetime())))
        ));
    }

}
