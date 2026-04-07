package com.tty.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.ServerArgs;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "server", permission = "ari.command.serverr", tokenLength = 2)
@LiteralCommand
public class server extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ServerArgs());
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
