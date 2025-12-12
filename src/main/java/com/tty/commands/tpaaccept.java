package com.tty.commands;

import com.tty.commands.args.tpa.TpaAcceptArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class tpaaccept extends BaseLiteralArgumentLiteralCommand {

    public tpaaccept() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaAcceptArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "tpaaccept";
    }

    @Override
    public String permission() {
        return "ari.command.tpaaccept";
    }
}
