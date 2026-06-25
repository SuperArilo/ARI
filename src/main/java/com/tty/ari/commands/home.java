package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.gui.home.HomeList;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "home", permission = "ari.command.home", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class home extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new GuiState(player, new HomeList(player)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigInstance().getValue("home.enable", FilePath.FUNCTION_CONFIG, Boolean.class, true);
    }
}
