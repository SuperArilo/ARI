package com.tty.commands;

import com.tty.commands.args.ItemNameArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class itemname extends BaseLiteralArgumentLiteralCommand {

    public itemname() {
        super(2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemNameArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    public String name() {
        return "itemname";
    }

    @Override
    public String permission() {
        return "ari.command.itemname";
    }
}
