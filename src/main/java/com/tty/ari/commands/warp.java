package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.configuration.warp.WarpConfig;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.gui.warp.WarpList;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "warp", permission = "ari.command.warp", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class warp extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Ari.instance.getStatusManager().get(GuiManagerStateService.class).addState(new GuiState(player, new WarpList(player)));
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(WarpConfig.class).isEnable();
    }
}
