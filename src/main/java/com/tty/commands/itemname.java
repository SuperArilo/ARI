package com.tty.commands;

import com.tty.commands.args.ItemNameArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "itemname", permission = "ari.command.itemname", tokenLength = 2)
@LiteralCommand
public class itemname extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemNameArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
