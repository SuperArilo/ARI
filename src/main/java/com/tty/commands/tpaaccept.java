package com.tty.commands;

import com.tty.commands.args.tpa.TpaAcceptArgs;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tpaaccept", permission = "ri.command.tpaaccept", tokenLength = 2)
@LiteralCommand
public class tpaaccept extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaAcceptArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
