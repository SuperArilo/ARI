package com.tty.commands.args.zako;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.ServerPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.enumType.FilePath;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.services.ConfigDataService;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ZakoInfoPlayer extends ZakoBaseArgs<String> {

    public ZakoInfoPlayer() {
        super(true, 3, StringArgumentType.string(), true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Set<String> strings = onlinePlayers.stream().map(Player::getName).collect(Collectors.toSet());
        if (onlinePlayers.isEmpty() || args.length != 3) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[2], strings));
    }

    @Override
    public String name() {
        return "name or uuid (string)";
    }

    @Override
    public String permission() {
        return "ari.command.zako.info";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String value = args[2];
        UUID uuid = this.parseUUID(value);
        if (uuid == null) return;

        String PATTERN_DATETIME = this.getPatternDatetime();

        PLAYER_MANAGER.getInstance(uuid.toString()).thenCombine(WHITELIST_MANAGER.getInstance(uuid.toString()), (playerInstance, whitelistInstance) -> {
            if (playerInstance == null || whitelistInstance == null) {
                sender.sendMessage(ConfigUtils.t("function.zako.zako-check-not-exist"));
                return null;
            }

            Map<String, Component> map = new HashMap<>();

            map.put(LangType.PLAYER_NAME.getType(), ComponentUtils.text(playerInstance.getPlayerName()));
            map.put(LangType.FIRST_LOGIN_SERVER_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(playerInstance.getFirstLoginTime(), PATTERN_DATETIME)));
            map.put(LangType.LAST_LOGIN_SERVER_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(playerInstance.getLastLoginOffTime(), PATTERN_DATETIME)));
            map.put(LangType.TOTAL_TIME_ON_SERVER.getType(), ComponentUtils.text(TimeFormatUtils.format(playerInstance.getTotalOnlineTime())));

            return new Result(map, playerInstance, whitelistInstance);
        })
        .thenAccept(result -> {
            if (result == null) return;
            Lib.Scheduler.run(Ari.instance,
                i -> {
                    Map<String, Component> map = result.map;
                    Player player = Bukkit.getPlayer(UUID.fromString(result.serverPlayer.getPlayerUUID()));
                    WhitelistInstance whitelistInstance = result.whitelistInstance;
                    map.put(LangType.PLAYER_WORLD.getType(), ComponentUtils.text(player == null ? Ari.instance.dataService.getValue("base.no-record"):player.getWorld().getName()));
                    map.put(LangType.PLAYER_LOCATION.getType(), ComponentUtils.text(player == null ? Ari.instance.dataService.getValue("base.no-record"): FormatUtils.XYZText(player.getX(), player.getY(), player.getZ())));

                    String operator;
                    if(whitelistInstance.getOperator().equals(Operator.CONSOLE.getUuid())) {
                        operator = "CONSOLE";
                    } else {
                        operator = Bukkit.getOfflinePlayer(UUID.fromString(whitelistInstance.getOperator())).getName();
                    }
                    map.put(LangType.ZAKO_WHITELIST_OPERATOR.getType(), ComponentUtils.text(operator == null ? "null":operator));
                    map.put(LangType.ZAKO_WHITELIST_ADD_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(whitelistInstance.getAddTime(), PATTERN_DATETIME)));

                    List<String> list = Ari.C_INSTANCE.getValue("server.player.info", FilePath.LANG, new TypeToken<List<String>>(){}.getType(), List.of());
                    sender.sendMessage(ComponentUtils.textList(list, map));
                });
        })
        .exceptionally(i -> {
            Log.error(i, "error");
            return null;
        });
    }

    private @NotNull String getPatternDatetime() {
        ConfigDataService service = Ari.instance.dataService;

        return "yyyy"
                + Objects.toString(service.getValue("base.time-format.year"), "")
                + "MM" + Objects.toString(service.getValue("base.time-format.month"), "")
                + "dd" + Objects.toString(service.getValue("base.time-format.day"), "")
                + "HH" + Objects.toString(service.getValue("base.time-format.hour"), "")
                + "mm" + Objects.toString(service.getValue("base.time-format.minute"), "")
                + "ss" + Objects.toString(service.getValue("base.time-format.second"), "");
    }

    private record Result(Map<String, Component> map, ServerPlayer serverPlayer, WhitelistInstance whitelistInstance) { }

}
