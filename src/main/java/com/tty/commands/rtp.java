package com.tty.commands;

import com.tty.Ari;
import com.tty.commands.sub.RtpCancel;
import com.tty.dto.state.teleport.RandomTpState;
import com.tty.enumType.FilePath;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.states.teleport.RandomTpStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class rtp extends BaseLiteralArgumentLiteralCommand {

    public rtp() {
        super(false, 1);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new RtpCancel(false));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.RTP_CONFIG.name()))) return;

        Player player = (Player) sender;
        Ari.instance.stateMachineManager
                .get(RandomTpStateService.class)
                .addState(new RandomTpState(
                        player,
                        Ari.C_INSTANCE.getValue("main.search-count", FilePath.RTP_CONFIG, Integer.class, 10),
                        player.getWorld()));
    }

    @Override
    public String name() {
        return "rtp";
    }

    @Override
    public String permission() {
        return "ari.command.rtp";
    }

}
