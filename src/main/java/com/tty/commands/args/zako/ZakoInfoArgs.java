package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.services.ConfigDataService;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ZakoInfoArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public ZakoInfoArgs() {
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
        UUID uuid = PublicFunctionUtils.parseUUID(value);
        if (uuid == null) return;
        Ari.PLACEHOLDER.renderList("server.player.info", Bukkit.getServer().getOfflinePlayer(uuid)).thenAccept(sender::sendMessage);
    }

    public static @NotNull String getPatternDatetime() {
        ConfigDataService service = Ari.DATA_SERVICE;
        return "yyyy"
                + Objects.toString(service.getValue("base.time-format.year"), "")
                + "MM" + Objects.toString(service.getValue("base.time-format.month"), "")
                + "dd" + Objects.toString(service.getValue("base.time-format.day"), "")
                + "HH" + Objects.toString(service.getValue("base.time-format.hour"), "")
                + "mm" + Objects.toString(service.getValue("base.time-format.minute"), "")
                + "ss" + Objects.toString(service.getValue("base.time-format.second"), "");
    }

}
