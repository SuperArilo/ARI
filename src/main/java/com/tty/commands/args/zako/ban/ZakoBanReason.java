package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoBanReason extends ZakoBanBase<String> {

    public ZakoBanReason() {
        super(true, 5, StringArgumentType.string(), false);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanDay());
    }

    @Override
    public String name() {
        return "\"reason\" (string)";
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
