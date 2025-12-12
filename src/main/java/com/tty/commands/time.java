package com.tty.commands;

import com.tty.commands.args.TimeArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class time extends BaseLiteralArgumentLiteralCommand {

    public time() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TimeArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "time";
    }

    @Override
    public String permission() {
        return "ari.command.time";
    }
}
