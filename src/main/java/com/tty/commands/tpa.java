package com.tty.commands;

import com.tty.commands.args.tpa.TpaArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tpa", permission = "ari.command.tpa", tokenLength = 2)
@LiteralCommand
public class tpa extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
