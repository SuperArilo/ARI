package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.lib.Log;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class ZakoBanDay extends BaseRequiredArgumentLiteralCommand<Integer> {

    protected ZakoBanDay() {
        super(IntegerArgumentType.integer(), false);
    }

    @Override
    public List<String> tabSuggestions() {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "day";
    }

    @Override
    public String permission() {
        return "ari.command.zako.ban";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Log.debug(Arrays.toString(args));
    }
}
