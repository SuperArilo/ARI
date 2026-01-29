package com.tty.commands.sub;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "reload", permission = "ari.command.reload", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class Reload extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("function.reload.doing")));
        Bukkit.getPluginManager().callEvent(new CustomPluginReloadEvent(sender));
    }

}
