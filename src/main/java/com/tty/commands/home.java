package com.tty.commands;

import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.gui.home.HomeList;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "home", permission = "ari.command.home", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class home extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.HOME_CONFIG.name()))) return;

        new HomeList((Player) sender).open();
    }

}
