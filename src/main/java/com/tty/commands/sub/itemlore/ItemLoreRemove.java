package com.tty.commands.sub.itemlore;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.itemlore.ItemLoreRemoveArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "remove", permission = "ari.command.itemlore.remove", tokenLength = 3)
@LiteralCommand
public class ItemLoreRemove extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreRemoveArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
