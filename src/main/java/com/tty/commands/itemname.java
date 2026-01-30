package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.ItemNameArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "itemname", permission = "ari.command.itemname", tokenLength = 2)
@LiteralCommand
public class itemname extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemNameArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
