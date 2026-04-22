package com.tty.ari.commands.args.tpa;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name (string)", permission = "ari.command.tparefuse", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class TpaRefuseArgs extends TpaBaseLiteralLiteralArgument {

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> strings = this.getResponseList(sender);
        if (args.length != 2) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], strings));
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (this.preCheckIsNotPass(sender, args)) return 0;

        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) return 0;
        this.checkAfterResponse(player, target, state -> {
            ConfigUtils.t("function.tpa.refuse-success", player).thenAccept(sender::sendMessage);
            Ari.PLACEHOLDER.render("function.tpa.refused", target).thenAccept(i ->
                    Ari.instance.getScheduler().runAtEntity(Ari.instance, target, task -> target.sendMessage(i), null));
        });
        return Command.SINGLE_SUCCESS;
    }
}
