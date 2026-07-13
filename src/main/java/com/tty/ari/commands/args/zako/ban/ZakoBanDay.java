package com.tty.ari.commands.args.zako.ban;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.RequiredArgumentCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "day (int)", permission = "ari.command.zako.ban", tokenLength = 8, allowConsole = true)
@ArgumentCommand
public class ZakoBanDay extends RequiredArgumentCommand<Integer> {

    @Override
    protected @NotNull ArgumentType<Integer> argumentType() {
        return IntegerArgumentType.integer(0);
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanHour());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(null);
    }
}
