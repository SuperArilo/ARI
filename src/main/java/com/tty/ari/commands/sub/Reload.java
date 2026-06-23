package com.tty.ari.commands.sub;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.ari.command.LiteralArgumentCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "reload", permission = "ari.command.reload", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class Reload extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("function.reload.doing")));
        Bukkit.getPluginManager().callEvent(new CustomPluginReloadEvent(Ari.instance, sender));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
