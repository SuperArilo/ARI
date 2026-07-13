package com.tty.ari.commands.sub.zako;

import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.zako.ban.ZakoBanPlayer;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "ban", permission = "ari.command.zako.ban", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoBan extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanPlayer());
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
