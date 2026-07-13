package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.ServerArgs;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "server", permission = "ari.command.server", tokenLength = 2)
@LiteralCommand
public class Server extends LiteralArgumentCommand {

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ServerArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
