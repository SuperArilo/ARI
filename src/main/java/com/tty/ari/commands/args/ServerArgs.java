package com.tty.ari.commands.args;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.ari.Ari;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.dto.BungeeCache;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


@CommandMeta(displayName = "server name (string)", permission = "ari.command.server", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class ServerArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return CompletableFuture.completedFuture(Set.of());
        if (args.length == 1) {
            if (BungeeCache.getState() == BungeeCache.State.UNKNOWN) {
                return BungeeCache.waitForLoad(2, () -> Ari.instance.getScheduler().runAsync(Ari.instance, i -> this.request(player)));
            } else if (BungeeCache.getState() == BungeeCache.State.READY) {
                return CompletableFuture.completedFuture(BungeeCache.getServers());
            }
        } else if (args.length == 2) {
            if(BungeeCache.getState() == BungeeCache.State.READY) return BungeeCache.getServers(args[1]);

        }
        return CompletableFuture.completedFuture(Set.of());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) return 0;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(args[1]);

        player.sendPluginMessage(Ari.instance, "BungeeCord", out.toByteArray());
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

    private void request(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServers");
        player.sendPluginMessage(Ari.instance, "BungeeCord", out.toByteArray());
    }

}
