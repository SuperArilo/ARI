package com.tty.ari.commands.args.tpa;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.ari.dto.state.teleport.PreEntityToEntityState;
import com.tty.ari.enumType.FilePath;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.teleport.PreTeleportStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name (string)", permission = "ari.command.tpa", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class TpaArgs extends TpaBaseLiteralLiteralArgument {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (this.preCheckIsNotPass(sender, args)) return 0;
        Player owner = (Player) sender;
        Player player = Ari.instance.getServer().getPlayerExact(args[1]);
        if (player != null) {
            Ari.STATE_MACHINE_MANAGER
                    .get(PreTeleportStateService.class)
                    .addState(new PreEntityToEntityState(
                            owner,
                            player,
                            TeleportType.TPA,
                            Ari.instance.getConfigInstance().getValue("main.teleport.request-expired-time", FilePath.TPA_CONFIG, Integer.class, 10)
                    ));
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(this.getExcludeMePlayerList(sender, args));
    }
}
