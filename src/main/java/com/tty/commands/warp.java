package com.tty.commands;

import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.gui.warp.WarpList;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class warp extends BaseLiteralArgumentLiteralCommand {

    public warp() {
        super(false, 1);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.WARP_CONFIG.name()))) return;

        new WarpList((Player) sender).open();
    }

    @Override
    public String name() {
        return "warp";
    }

    @Override
    public String permission() {
        return "ari.command.warp";
    }
}
