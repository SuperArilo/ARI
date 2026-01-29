package com.tty.commands;

import com.tty.commands.args.tpa.TpaRefuseArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tparefuse", permission = "ari.command.tparefuse", tokenLength = 2)
@LiteralCommand
public class tparefuse extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaRefuseArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
