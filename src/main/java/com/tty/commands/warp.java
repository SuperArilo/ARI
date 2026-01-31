package com.tty.commands;

import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.enumType.FilePath;
import com.tty.gui.warp.WarpList;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
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
        player.openInventory(new WarpList(player).getInventory());
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.C_INSTANCE.getObject(FilePath.WARP_CONFIG.name()));
    }
}
