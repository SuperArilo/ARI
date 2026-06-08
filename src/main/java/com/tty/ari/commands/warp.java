package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.gui.warp.WarpList;
import com.tty.ari.states.GuiManagerStateService;
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
    public int execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new GuiState(player, new WarpList(player)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.instance.getConfigInstance().getObject(FilePath.WARP_CONFIG.name()));
    }
}
