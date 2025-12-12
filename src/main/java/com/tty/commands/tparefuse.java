package com.tty.commands;

import com.tty.commands.args.tpa.TpaRefuseArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class tparefuse extends BaseLiteralArgumentLiteralCommand {

    public tparefuse() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaRefuseArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "tparefuse";
    }

    @Override
    public String permission() {
        return "ari.command.tparefuse";
    }
}
