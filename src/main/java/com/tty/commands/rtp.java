package com.tty.commands;

import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.sub.RtpCancel;
import com.tty.dto.state.teleport.RandomTpState;
import com.tty.enumType.FilePath;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.states.teleport.RandomTpStateService;
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
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.RTP_CONFIG.name()))) return;
        Player player = (Player) sender;
        Ari.STATE_MACHINE_MANAGER
                .get(RandomTpStateService.class)
                .addState(new RandomTpState(
                        player,
                        Ari.C_INSTANCE.getValue("main.search-count", FilePath.RTP_CONFIG, Integer.class, 10),
                        player.getWorld()));
    }

}
