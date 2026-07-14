package com.tty.ari.commands.sub;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "about", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class About extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        ConfigUtils.tList("server.message.about").thenAccept(sender::sendMessage);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
