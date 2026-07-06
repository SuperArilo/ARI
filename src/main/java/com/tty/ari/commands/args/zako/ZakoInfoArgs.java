package com.tty.ari.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.ari.Ari;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.ServerPlayer;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandMeta(displayName = "name or uuid (string)", permission = "ari.command.zako.info", tokenLength = 3, allowConsole = true)
@ArgumentCommand(isSuggests = true)
public class ZakoInfoArgs extends RequiredArgumentCommand<String> {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        Set<String> strings = onlinePlayers.stream().map(Player::getName).collect(Collectors.toSet());
        if (onlinePlayers.isEmpty() || args.length != 3) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[2], strings));
    }

    @Override
    public int execute(CommandSender sender, String[] args) {

        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);

        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return 0;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        Ari.REPOSITORY_MANAGER.get(ServerPlayer.class).get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid.toString()), PartitionKey.global()).thenCompose(i -> {
           if (i == null) {
               CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.zako-check-not-exist", player):ConfigUtils.t("function.zako.zako-check-not-exist");
               return future.thenAccept(sender::sendMessage).thenApply(t -> null);
           }
           return ConfigUtils.tList("server.player.info", PlayerCache.getPlayer(uuid));
        }).thenAccept(message -> {
            if (message != null) {
                sender.sendMessage(message);
            }
        }).exceptionally(e -> {
            Ari.instance.getLog().error(e);
            if (sender instanceof Player player) {
                ConfigUtils.t("function.zako.list-request-error", player).thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t("function.zako.list-request-error").thenAccept(sender::sendMessage);
            }
            return null;
        });
        return Command.SINGLE_SUCCESS;

    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
