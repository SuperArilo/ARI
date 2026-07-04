package com.tty.ari.commands.args.zako;

import com.mojang.brigadier.Command;
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

import static com.tty.ari.commands.sub.zako.ZakoBanList.Build_Zako_Ban_List;

@CommandMeta(displayName = "page (int)", permission = "ari.command.zako.banlist", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoBanListArgs extends RequiredArgumentCommand<Integer> {

    @Override
    protected @NotNull ArgumentType<Integer> argumentType() {
        return IntegerArgumentType.integer(1);
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
    public int execute(CommandSender sender, String[] args) {
        Build_Zako_Ban_List(sender, Integer.parseInt(args[2]));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
