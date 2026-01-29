package com.tty.commands;

import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.gui.warp.WarpList;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "warp", permission = "ari.command.warp", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class warp extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.WARP_CONFIG.name()))) return;
        new WarpList((Player) sender).open();
    }

}
