package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.ItemNameArgs;
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
    public int execute(CommandSender sender, String[] args) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
