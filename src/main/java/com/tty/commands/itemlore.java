package com.tty.commands;

import com.tty.commands.sub.itemlore.ItemLoreAdd;
import com.tty.commands.sub.itemlore.ItemLoreRemove;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "itemlore", permission = "ari.command.itemlore", tokenLength = 2)
@LiteralCommand
public class itemlore extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreAdd(),
                new ItemLoreRemove());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
