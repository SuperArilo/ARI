package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.command.RequiredArgumentCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.tty.commands.sub.zako.ZakoList.Build_Zako_List;

@CommandMeta(displayName = "page (int)", permission = "ari.command.zako.list", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoListArgs extends RequiredArgumentCommand<Integer> {

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
    public void execute(CommandSender sender, String[] args) {
        Build_Zako_List(sender, Integer.parseInt(args[2]));
    }
}
