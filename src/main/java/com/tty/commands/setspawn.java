package com.tty.commands;

import com.mojang.brigadier.Command;
import com.tty.Ari;
import com.tty.api.utils.ComponentUtils;
import com.tty.command.LiteralArgumentCommand;
import com.tty.dto.SpawnLocation;
import com.tty.enumType.FilePath;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

@CommandMeta(displayName = "setspawn", permission = "ari.command.setspawn", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class setspawn extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        Location location = player.getLocation();
        SpawnLocation spawnLocation = new SpawnLocation();
        spawnLocation.setWorldName(player.getWorld().getName());
        spawnLocation.setX(location.getX());
        spawnLocation.setY(location.getY());
        spawnLocation.setZ(location.getZ());
        spawnLocation.setPitch(location.getPitch());
        spawnLocation.setYaw(location.getYaw());

        try {
            Ari.instance.getConfigInstance().setValue(Ari.instance, "main.location", FilePath.SPAWN_CONFIG, spawnLocation.toMap());
        } catch (IOException e) {
            sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-error")));
            Ari.instance.getLog().error(e);
        }

        ConfigUtils.t("function.spawn.create-success", player).thenAccept(player::sendMessage);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.instance.getConfigInstance().getObject(FilePath.SPAWN_CONFIG.name()));
    }

}
