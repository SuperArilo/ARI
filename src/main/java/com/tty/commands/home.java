package com.tty.commands;

import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.gui.home.HomeList;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class home extends BaseLiteralArgumentLiteralCommand {

    public home() {
        super(false, 1);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.HOME_CONFIG.name()))) return;

        new HomeList((Player) sender).open();
    }

    @Override
    public String name() {
        return "home";
    }

    @Override
    public String permission() {
        return "ari.command.home";
    }
}
