package com.tty.ari.commands;

import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.itemlore.ItemLoreAdd;
import com.tty.ari.commands.sub.itemlore.ItemLoreRemove;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "itemlore", permission = "ari.command.itemlore", tokenLength = 2)
@LiteralCommand
public class itemlore extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemLoreAdd(), new ItemLoreRemove());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
