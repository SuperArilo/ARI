package com.tty.ari.commands.args.nick;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.persistence.CachedRepository;
import com.tty.api.persistence.query.Patch;
import com.tty.api.persistence.query.Query;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import com.tty.ari.tool.PlayerCache;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class PublicNickSetArgs extends RequiredArgumentCommand<String> {
    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

    protected void setValue(CommandSender sender,  String[] args, PlaceholderPlayer placeholder) {
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);
        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return;
        }
        String uuid = offlinePlayer.getUniqueId().toString();
        String value = args[4];
        CachedRepository<ServerPlayer, Integer> players = Ari.REPOSITORY_MANAGER.players();
        Query<ServerPlayer> query = Query.of(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid);
        players.findOne(query).thenCompose(serverPlayer -> {
            if (serverPlayer.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }
            return switch (placeholder) {
                case PLAYER_NAME_PREFIX -> players.update(query,
                        Patch.of(ServerPlayer.class).set(ServerPlayer::getNamePrefix, value)).thenApply(count -> count > 0);
                case PLAYER_NAME_SUFFIX -> players.update(query,
                        Patch.of(ServerPlayer.class).set(ServerPlayer::getNameSuffix, value)).thenApply(count -> count > 0);
                default -> CompletableFuture.completedFuture(false);
            };
        }).thenAccept(status -> {
            if (status) {
                sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.command.execute-success"), offlinePlayer));
            } else {
                Component append = Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.command.execute-success"), offlinePlayer).append(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-player.not-exist"), offlinePlayer));
                sender.sendMessage(append);
            }
        }).exceptionally(e -> {
            sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-error"), offlinePlayer));
            Ari.instance.getLog().error(e);
            return null;
        });
    }

}
