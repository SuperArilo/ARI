package com.tty.commands;

import com.tty.commands.args.tpa.TpaHereArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tpahere", permission = "ari.command.tpahere", tokenLength = 2)
@LiteralCommand
public class tpahere extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaHereArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
