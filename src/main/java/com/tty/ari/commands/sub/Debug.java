package com.tty.ari.commands.sub;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "debug", permission = "ari.command.debug", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class Debug extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean enabled = !Ari.instance.isDebug();
        Ari.instance.getConfig().set("debug.enable", enabled);
        Ari.instance.saveConfig();
        Ari.instance.setDebug(enabled);

        String key = enabled ? "function.debug.enabled" : "function.debug.disabled";
        if (sender instanceof Player player) {
            ConfigUtils.t(key, player).thenAccept(sender::sendMessage);
        } else {
            ConfigUtils.t(key).thenAccept(sender::sendMessage);
        }
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
