package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.api.annotations.ArgumentCommand;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
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
    public void execute(CommandSender sender, String[] args) {
        if (this.preCheckIsNotPass(sender, args)) return;

        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[1]);
        this.checkAfterResponse(player, target, state -> {
            ConfigUtils.t("function.tpa.refuse-success", player).thenAccept(sender::sendMessage);
            assert target != null;
            Ari.PLACEHOLDER.render("function.tpa.refused", target).thenAccept(i ->
                    Ari.SCHEDULER.runAtEntity(Ari.instance, target, task -> target.sendMessage(i), null));
        });
    }
}
