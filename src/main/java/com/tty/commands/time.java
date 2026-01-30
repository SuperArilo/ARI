package com.tty.commands;

import com.tty.commands.args.TimeArgs;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "time", permission = "ari.command.time", tokenLength = 2)
@LiteralCommand
public class time extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TimeArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
