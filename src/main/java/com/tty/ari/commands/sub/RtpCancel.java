package com.tty.ari.commands.sub;

import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.states.teleport.RandomTpStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "cancel", permission = "ari.command.rtp.cancel", tokenLength = 2)
@LiteralCommand(directExecute = true)
public class RtpCancel extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        RandomTpStateService machine = Ari.instance.getStatusManager().get(RandomTpStateService.class);
        List<RandomTpState> states = machine.getStates((Entity) sender);
        if (states.isEmpty()) {
            ConfigUtils.t("function.rtp.no-rtp", player).thenAccept(player::sendMessage);
            return;
        }
        for (RandomTpState state : states) {
            machine.stopState(state);
        }
        ConfigUtils.t("function.rtp.rtp-cancel", player).thenAccept(player::sendMessage);
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
