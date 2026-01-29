package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "\"reason\" (string)", permission = "ari.command.zako.ban",  tokenLength = 5)
@ArgumentCommand
public class ZakoBanReason extends ZakoBanBase<String> {

    public ZakoBanReason() {
        super(StringArgumentType.string());
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(Set.of());
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanDay());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.ban(sender, args);
    }
}
