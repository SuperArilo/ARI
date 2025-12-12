package com.tty.commands;

import com.tty.commands.args.SetHomeArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class sethome extends BaseLiteralArgumentLiteralCommand {

    public sethome() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetHomeArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "sethome";
    }

    @Override
    public String permission() {
        return "ari.command.sethome";
    }
}
