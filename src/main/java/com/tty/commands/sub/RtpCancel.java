package com.tty.commands.sub;

import com.tty.Ari;
import com.tty.dto.state.teleport.RandomTpState;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "cancel", permission = "ari.command.rtp.cancel", tokenLength = 2)
@LiteralCommand(directExecute = true)
public class RtpCancel extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        RandomTpStateService machine = Ari.STATE_MACHINE_MANAGER.get(RandomTpStateService.class);
        List<RandomTpState> states = machine.getStates((Entity) sender);
        if (states.isEmpty()) {
            ConfigUtils.t("function.rtp.no-rtp", player).thenAccept(player::sendMessage);
            return;
        }
        if(machine.removeState(states.getFirst())) {
            ConfigUtils.t("function.rtp.rtp-cancel", player).thenAccept(player::sendMessage);
        }
    }

}
