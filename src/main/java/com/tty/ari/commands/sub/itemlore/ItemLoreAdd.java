package com.tty.ari.commands.sub.itemlore;

import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.itemlore.ItemloreAddArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "add", permission = "ari.command.itemlore.add", tokenLength = 3)
@LiteralCommand
public class ItemLoreAdd extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ItemloreAddArgs());
    }

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
