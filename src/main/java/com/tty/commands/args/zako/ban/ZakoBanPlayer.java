package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

public class ZakoBanPlayer extends ZakoBanBase<String> {

    public ZakoBanPlayer() {
        super(true, 4, StringArgumentType.string(), true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanReason());
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        Stream<String> stream = Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(i -> !i.equals(sender.getName()));
        if (args.length == 2) return stream.toList();
        List<String> list = stream.filter(i -> i.startsWith(args[2])).toList();
        if (list.isEmpty()) return List.of("<player name or uuid (string)>");
        return list;
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
