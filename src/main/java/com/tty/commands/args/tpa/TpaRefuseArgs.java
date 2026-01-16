package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TpaRefuseArgs extends TpaBaseLiteralLiteralArgument {

    public TpaRefuseArgs() {
        super(2);
    }

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
    public String name() {
        return "name (string)";
    }

    @Override
    public String permission() {
        return "ari.command.tparefuse";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (this.preCheckIsNotPass(sender, args)) return;

        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[1]);
        if (this.checkAfterResponse(player, target) != null) {
            sender.sendMessage(ConfigUtils.t("function.tpa.refuse-success"));
            assert target != null;
            Ari.PLACEHOLDER.render("function.tpa.refused", player).thenAccept(target::sendMessage);
        }
    }
}
