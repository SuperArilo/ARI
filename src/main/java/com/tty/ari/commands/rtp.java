package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.RtpCancel;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.teleport.RandomTpStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "rtp", permission = "ari.command.rtp", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class rtp extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new RtpCancel());
    }

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Ari.instance.getStatusManager().get(RandomTpStateService.class).addState(
                new RandomTpState(
                        player,
                        Ari.instance.getConfigurationManager().get(FunctionConfig.class).getRtpSearchCount(),
                        player.getWorld())
        );

        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(FunctionConfig.class).isEnable(TeleportType.RTP);
    }
}
