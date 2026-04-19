package com.tty.commands.args.zako.add;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "\"remark\" (string)", permission = "ari.command.zako.add", tokenLength = 4, allowConsole = true)
@ArgumentCommand
public class ZakoAddAndRemarkArgs extends ZakoAddBase<String>{

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        return this.addPlayer(sender, args);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

}
