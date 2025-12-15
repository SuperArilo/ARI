package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoBanMinute extends ZakoBanBase<Integer>{

    public ZakoBanMinute() {
        super(true, 7, IntegerArgumentType.integer(), false);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanSecond());
    }

    @Override
    public String name() {
        return "minute (int)";
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
