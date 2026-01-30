package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.tpa.TpaHereArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tpahere", permission = "ari.command.tpahere", tokenLength = 2)
@LiteralCommand
public class tpahere extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaHereArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
