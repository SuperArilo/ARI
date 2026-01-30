package com.tty.commands.sub;

import com.tty.Ari;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.event.CustomPluginReloadEvent;
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
        sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("function.reload.doing")));
        Bukkit.getPluginManager().callEvent(new CustomPluginReloadEvent(sender));
    }

}
