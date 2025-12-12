package com.tty.commands;

import com.tty.commands.args.tpa.TpaHereArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class tpahere extends BaseLiteralArgumentLiteralCommand {

    public tpahere() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaHereArgs(this.name(), this.permission()));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    public String name() {
        return "tpahere";
    }

    @Override
    public String permission() {
        return "ari.command.tpahere";
    }
}
