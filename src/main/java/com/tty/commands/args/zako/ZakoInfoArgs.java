package com.tty.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.ServerPlayer;
import com.tty.lib.Log;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.services.ConfigDataService;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
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
        Ari.REPOSITORY_MANAGER.get(ServerPlayer.class).get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid.toString())).thenCompose(i -> {
           if (i == null) {
               CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.zako-check-not-exist", player):ConfigUtils.t("function.zako.zako-check-not-exist");
               return future.thenAccept(sender::sendMessage).thenApply(t -> null);
           }
           return Ari.PLACEHOLDER.renderList("server.player.info", Bukkit.getServer().getOfflinePlayer(uuid));
        }).thenAccept(message -> {
            if (message != null) {
                sender.sendMessage(message);
            }
        }).exceptionally(e -> {
            Log.error(e);
            if (sender instanceof Player player) {
                ConfigUtils.t("function.zako.list-request-error", player).thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t("function.zako.list-request-error").thenAccept(sender::sendMessage);
            }
            return null;
        });

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
