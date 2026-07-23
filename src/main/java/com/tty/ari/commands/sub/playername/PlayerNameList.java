package com.tty.ari.commands.sub.playername;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.RequiredArgumentCommand;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandMeta(displayName = "player name or uuid (string)", permission = "ari.command.playername", tokenLength = 2, allowConsole = true)
@ArgumentCommand(isSuggests = true)
public class PlayerNameList extends RequiredArgumentCommand<PlayerSelectorArgumentResolver> {

    @Override
    protected @NotNull ArgumentType<PlayerSelectorArgumentResolver> argumentType() {
        return ArgumentTypes.player();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<@NotNull String> collect = Bukkit.getServer().getOnlinePlayers().stream().map(i -> i.getName()).collect(Collectors.toSet());
        if (args.length == 1 && collect.isEmpty()) return CompletableFuture.completedFuture(Set.of("<player name or uuid (string)>"));
        if (args.length == 1) return CompletableFuture.completedFuture(collect);
        String arg = args[1];
        Set<String> filtered = collect.stream()
                .filter(name -> name.startsWith(arg))
                .collect(Collectors.toSet());
        if (filtered.isEmpty()) return CompletableFuture.completedFuture(Set.of("<player name or uuid (string)>"));
        return CompletableFuture.completedFuture(filtered);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new NamePrefix(), new NameSuffix());
    }

    @Override
    protected boolean isEnableInGame() {
        return false;
    }

}
