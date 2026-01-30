package com.tty.commands.sub.itemlore;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.itemlore.ItemloreAddArgs;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;

import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "add", permission = "ari.command.itemlore.add", tokenLength = 3)
@LiteralCommand
public class ItemLoreAdd extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemloreAddArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
