package com.tty.commands;

import com.tty.commands.sub.itemlore.ItemLoreAdd;
import com.tty.commands.sub.itemlore.ItemLoreRemove;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class itemlore extends BaseLiteralArgumentLiteralCommand {

    public itemlore() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreAdd(),
                new ItemLoreRemove());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }


    @Override
    public String name() {
        return "itemlore";
    }

    @Override
    public String permission() {
        return "ari.command.itemlore";
    }
}
