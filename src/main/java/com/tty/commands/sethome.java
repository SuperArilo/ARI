package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.SetHomeArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "sethome", permission = "ari.command.sethome", tokenLength = 2)
@LiteralCommand
public class sethome extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetHomeArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
