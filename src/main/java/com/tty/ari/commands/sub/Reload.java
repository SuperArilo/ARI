package com.tty.ari.commands.sub;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
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
    public void execute(CommandSender sender, String[] args) {
        Ari.instance.reload(sender);
        sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("function.reload.doing")));
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
