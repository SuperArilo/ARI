package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.sub.itemlore.ItemLoreAdd;
import com.tty.commands.sub.itemlore.ItemLoreRemove;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "itemlore", permission = "ari.command.itemlore", tokenLength = 2)
@LiteralCommand
public class itemlore extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreAdd(),
                new ItemLoreRemove());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
