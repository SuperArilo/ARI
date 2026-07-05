package com.tty.ari.commands.args;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.states.PlayerAFKService;
import com.tty.ari.tool.PlayerCache;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "player_name or uuid (string)", permission = "ari.command.afk.player", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class AfkArgs extends RequiredArgumentCommand<PlayerSelectorArgumentResolver> {

    @Override
    protected @NotNull ArgumentType<PlayerSelectorArgumentResolver> argumentType() {
        return ArgumentTypes.player();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(this.getExcludeMePlayerList(sender, args));
    }

    @Override
    public int execute(CommandSender sender, String[] args) {

        UUID uuid = PublicFunctionUtils.parseUUID(args[1]);
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(uuid);
        if (!(offlinePlayer instanceof Player player)) {
            String value = Ari.DATA_SERVICE.getValue("base.on-player.unable-player");
            sender.sendMessage(Ari.instance.getComponentTool().text(value));
            return 0;
        }

        for (PlayerAFKState state : Ari.instance.getStatusManager().get(PlayerAFKService.class).getStates(player)) {
            if (state.isAFK()) {
                state.resetStandCount();
            } else {
                state.afkNow();
            }
        }

        sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.command.execute-success")));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
