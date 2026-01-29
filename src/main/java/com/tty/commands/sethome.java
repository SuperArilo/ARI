package com.tty.commands;

import com.tty.commands.args.SetHomeArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "sethome", permission = "ari.command.sethome", tokenLength = 2)
@LiteralCommand
public class sethome extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetHomeArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
