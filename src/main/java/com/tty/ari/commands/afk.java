package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.AfkArgs;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.states.PlayerAFKService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "afk", permission = "ari.command.afk", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class afk extends LiteralArgumentCommand {
    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return CompletableFuture.completedFuture(null);
        for (PlayerAFKState state : Ari.instance.getStatusManager().get(PlayerAFKService.class).getStates(player)) {
            if (state.isAFK()) continue;
            state.afkNow();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new AfkArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
