package com.tty.commands.sub.itemlore;

import com.tty.commands.args.itemlore.ItemLoreRemoveArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ItemLoreRemove extends BaseLiteralArgumentLiteralCommand {

    public ItemLoreRemove() {
        super(false, 3);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreRemoveArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public String permission() {
        return "ari.command.itemlore.remove";
    }
}
