package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ZakoBanDay extends ZakoBanBase<Integer> {

    protected ZakoBanDay() {
        super(true, 5, IntegerArgumentType.integer(), false);
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(Set.of());
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanHour());
    }

    @Override
    public String name() {
        return "day (int)";
    }

    @Override
    public String permission() {
        return "ari.command.zako.ban";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.ban(sender, args);
    }
}
