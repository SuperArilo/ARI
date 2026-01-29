package com.tty.commands.sub.itemlore;

import com.tty.commands.args.itemlore.ItemLoreRemoveArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "remove", permission = "ari.command.itemlore.remove", tokenLength = 3)
@LiteralCommand
public class ItemLoreRemove extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreRemoveArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
