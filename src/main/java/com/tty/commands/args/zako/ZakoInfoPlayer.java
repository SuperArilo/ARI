package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.Log;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ZakoInfoPlayer extends BaseRequiredArgumentLiteralCommand<String> {

    public ZakoInfoPlayer() {
        super(StringArgumentType.string(), true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public List<String> tabSuggestions() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) return List.of("<name or uuid (string)>");
        List<String> list = new ArrayList<>();
        for (Player player : onlinePlayers) {
            list.add(player.getName());
        }
        return list;
    }

    @Override
    public String name() {
        return "<name or uuid (string)>";
    }

    @Override
    public String permission() {
        return "ari.command.zako.info";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Log.debug(Arrays.toString(args));
    }

}
