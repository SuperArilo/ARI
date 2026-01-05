package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ZakoBanPlayer extends ZakoBanBase<String> {

    public ZakoBanPlayer() {
        super(true, 4, StringArgumentType.string(), true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanReason());
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> players = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equals(sender.getName()))
                .collect(Collectors.toSet());

        if (args.length == 2) {
            return CompletableFuture.completedFuture(players);
        }

        if (args.length < 3) {
            return CompletableFuture.completedFuture(players);
        }

        String prefix = args[2];

        Set<String> filtered = players.stream()
                .filter(name -> name.startsWith(prefix))
                .collect(Collectors.toSet());

        if (filtered.isEmpty()) return CompletableFuture.completedFuture(Set.of("<player name or uuid (string)>"));
        return CompletableFuture.completedFuture(filtered);
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
