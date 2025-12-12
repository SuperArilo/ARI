package com.tty.commands.sub.itemlore;

import com.tty.commands.args.itemlore.ItemloreAddArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;

import org.bukkit.command.CommandSender;

import java.util.List;

public class ItemLoreAdd extends BaseLiteralArgumentLiteralCommand {

    public ItemLoreAdd() {
        super(false, 3);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemloreAddArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "add";
    }

    @Override
    public String permission() {
        return "ari.command.itemlore.add";
    }

}
