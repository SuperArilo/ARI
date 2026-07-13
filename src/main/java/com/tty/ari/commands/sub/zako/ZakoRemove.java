package com.tty.ari.commands.sub.zako;

import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.zako.ZakoRemoveArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "remove", permission = "ari.command.zako.remove", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoRemove extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoRemoveArgs());
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
