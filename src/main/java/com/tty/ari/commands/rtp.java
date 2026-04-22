package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.RtpCancel;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.ari.enumType.FilePath;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.states.teleport.RandomTpStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "rtp", permission = "ari.command.rtp", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class rtp extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new RtpCancel());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Ari.STATE_MACHINE_MANAGER
                .get(RandomTpStateService.class)
                .addState(new RandomTpState(
                        player,
                        Ari.instance.getConfigInstance().getValue("main.search-count", FilePath.RTP_CONFIG, Integer.class, 10),
                        player.getWorld()));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.instance.getConfigInstance().getObject(FilePath.RTP_CONFIG.name()));
    }
}
