package com.tty.ari.commands.args.zako.ban;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.RequiredArgumentCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "player name or uuid (string)", permission = "ari.command.zako.ban", tokenLength = 3, allowConsole = true)
@ArgumentCommand(isSuggests = true)
public class ZakoBanPlayer extends RequiredArgumentCommand<String> {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanDay());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        if (args.length == 2) return RequiredArgumentCommand.getPlayerList(sender, "", false);
        return RequiredArgumentCommand.getPlayerList(sender, args[2], false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }
}
