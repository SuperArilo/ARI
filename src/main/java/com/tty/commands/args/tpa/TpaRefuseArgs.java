package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.enumType.FilePath;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TpaRefuseArgs extends TpaBaseLiteralLiteralArgument {

    public TpaRefuseArgs(String name, String permission) {
        super(name, permission);
    }

    @Override
    public List<String> tabSuggestions() {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String permission() {
        return "";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.TPA_CONFIG.name()))) return;

        Player player = (Player) sender;
        Player target = Bukkit.getPlayerExact(args[1]);
        if (this.checkAfterResponse(player, target) != null) {
            sender.sendMessage(ConfigUtils.t("function.tpa.refuse-success"));
            assert target != null;
            target.sendMessage(ConfigUtils.t("function.tpa.refused", Map.of(LangType.TPA_BE_SENDER.getType(), Component.text(sender.getName()))));
        }
    }
}
