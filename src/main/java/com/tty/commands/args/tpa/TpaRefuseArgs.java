package com.tty.commands.args.tpa;

import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TpaRefuseArgs extends TpaBaseLiteralLiteralArgument {

    public TpaRefuseArgs() {
        super(false, 2);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        List<String> strings = this.getResponseList(sender);
        if (args.length != 2) return strings;
        return PublicFunctionUtils.tabList(args[1], strings);
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
            target.sendMessage(ConfigUtils.t("function.tpa.refused", Map.of(LangType.TPA_BE_SENDER.getType(), Component.text(sender.getName()))));
        }
    }
}
