package com.tty.ari.commands.sub.zako;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.zako.ZakoRemoveProfileArgs;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "removeprofile", permission = "ari.command.zako.removeprofile", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoRemoveProfile extends LiteralArgumentCommand {

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoRemoveProfileArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
