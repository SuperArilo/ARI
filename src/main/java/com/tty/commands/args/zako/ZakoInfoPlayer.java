package com.tty.commands.args.zako;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
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

        this.playerManager.getInstance(uuid.toString())
            .thenAccept(instance -> {
                if(instance == null) {
                    sender.sendMessage(ConfigUtils.t("function.zako.zako-check-not-exist"));
                    return;
                }
                Map<String, Component> map = new HashMap<>();
                ConfigDataService service = Ari.instance.dataService;

                String PATTERN_DATETIME = "yyyy" + service.getValue("base.time-format.year") + "MM" + service.getValue("base.time-format.month") + "dd" + service.getValue("base.time-format.day") + "HH" + service.getValue("base.time-format.hour") +"mm" + service.getValue("base.time-format.minute") +"ss" + service.getValue("base.time-format.second");

                map.put(LangType.PLAYER_NAME.getType(), ComponentUtils.text(instance.getPlayerName()));
                map.put(LangType.FIRST_LOGIN_SERVER_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(instance.getFirstLoginTime(), PATTERN_DATETIME)));
                map.put(LangType.LAST_LOGIN_SERVER_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(instance.getLastLoginOffTime(), PATTERN_DATETIME)));
                map.put(LangType.TOTAL_ON_SERVER.getType(), ComponentUtils.text(TimeFormatUtils.format(instance.getTotalOnlineTime())));
                Player player = Bukkit.getPlayer(UUID.fromString(instance.getPlayerUUID()));
                map.put(LangType.PLAYER_WORLD.getType(), ComponentUtils.text(player == null ? Ari.instance.dataService.getValue("base.no-record"):player.getWorld().getName()));
                map.put(LangType.PLAYER_LOCATION.getType(), ComponentUtils.text(player == null ? Ari.instance.dataService.getValue("base.no-record"): FormatUtils.XYZText(player.getX(), player.getY(), player.getZ())));

                List<String> list = Ari.C_INSTANCE.getValue("server.player.info", FilePath.LANG, new TypeToken<List<String>>(){}.getType(), List.of());

                sender.sendMessage(ComponentUtils.textList(list, map));
            }).exceptionally(i -> {
                Log.error(i, "error");
                return null;
            });
    }

}
