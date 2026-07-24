package com.tty.ari.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.api.ComponentTool;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.tool.PlayerCache;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "tp", permission = "ari.command.tp", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class TeleportToPlayerArgs extends RequiredArgumentCommand<PlayerSelectorArgumentResolver> {

    @Override
    protected @NotNull ArgumentType<PlayerSelectorArgumentResolver> argumentType() {
        return ArgumentTypes.player();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        if (args.length == 1) return RequiredArgumentCommand.getPlayerList(sender, "", true);
        return RequiredArgumentCommand.getPlayerList(sender, args[1], true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);
        if (!(offlinePlayer instanceof Player target)) {
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.unable-player"), player));
            return;
        }
        Ari.TELEPORTING_SERVICE.teleport(player, target.getLocation());
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
