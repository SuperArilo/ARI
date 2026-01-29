package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandMeta(displayName = "player name or uuid (string)", permission = "ari.command.zako.ban", tokenLength = 4, allowConsole = true)
@ArgumentCommand(isSuggests = true)
public class ZakoBanPlayer extends ZakoBanBase<String> {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanReason());
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> players = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equals(sender.getName()))
                .collect(Collectors.toSet());

        if (args.length == 2 && players.isEmpty()) return CompletableFuture.completedFuture(Set.of("<player name or uuid (string)>"));
        if (args.length == 2) return CompletableFuture.completedFuture(players);
        String prefix = args[2];
        Set<String> filtered = players.stream()
                .filter(name -> name.startsWith(prefix))
                .collect(Collectors.toSet());
        if (filtered.isEmpty()) return CompletableFuture.completedFuture(Set.of("<player name or uuid (string)>"));
        return CompletableFuture.completedFuture(filtered);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
