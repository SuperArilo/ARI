package com.tty.commands;

import com.tty.commands.args.SetWarpArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class setwarp extends BaseLiteralArgumentLiteralCommand {

    public setwarp() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetWarpArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "setwarp";
    }

    @Override
    public String permission() {
        return "ari.command.setwarp";
    }
}
