package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoBanPlayer extends ZakoBanBase<String> {

    public ZakoBanPlayer() {
        super(true, 3, StringArgumentType.string(), false);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanReason());
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String name() {
        return "player name or uuid (string)";
    }

    @Override
    public String permission() {
        return "ari.command.zako.ban";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
