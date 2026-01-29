package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.enumType.FilePath;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.enumType.TeleportType;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.states.teleport.PreTeleportStateService;
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
    public void execute(CommandSender sender, String[] args) {
        if (this.preCheckIsNotPass(sender, args)) return;
        Player owner = (Player) sender;
        Player player = Ari.instance.getServer().getPlayerExact(args[1]);
        Ari.STATE_MACHINE_MANAGER
                .get(PreTeleportStateService.class)
                .addState(new PreEntityToEntityState(
                        owner,
                        player,
                        TeleportType.TPA,
                        Ari.C_INSTANCE.getValue("main.teleport.request-expired-time", FilePath.TPA_CONFIG, Integer.class, 10)
                ));
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> strings = this.getOnlinePlayers(sender);
        if (args.length != 2) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], strings));
    }
}
