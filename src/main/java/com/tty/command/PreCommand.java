package com.tty.command;

import com.tty.Ari;
import com.tty.api.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class PreCommand extends AbstractSubCommand {

    protected boolean isDisabledInGame(CommandSender sender, YamlConfiguration configuration) {
        boolean b = configuration.getBoolean("main.enable", true);
        if (!b) {
            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.command.disabled")));
        }
        return b;
    }

}
