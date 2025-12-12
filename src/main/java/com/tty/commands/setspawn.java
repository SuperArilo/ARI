package com.tty.commands;

import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class setspawn extends BaseLiteralArgumentLiteralCommand {

    public setspawn() {
        super(false, 1);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.SPAWN_CONFIG.name()))) return;

        Player player = (Player) sender;
        Location location = player.getLocation();
        Ari.C_INSTANCE.setValue(Ari.instance, "main.location", FilePath.SPAWN_CONFIG, location);
        player.sendMessage(ConfigUtils.t("function.spawn.create-success"));
    }

    @Override
    public String name() {
        return "setspawn";
    }

    @Override
    public String permission() {
        return "ari.command.setspawn";
    }
}
