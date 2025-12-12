package com.tty.commands;

import com.tty.commands.args.tpa.TpaArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class tpa extends BaseLiteralArgumentLiteralCommand {

    public tpa() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "tpa";
    }

    @Override
    public String permission() {
        return "ari.command.tpa";
    }
}
