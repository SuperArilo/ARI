package com.tty.commands;

import com.tty.Ari;
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
    public void execute(CommandSender sender, String[] args) {

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
            Ari.C_INSTANCE.setValue(Ari.instance, "main.location", FilePath.SPAWN_CONFIG, spawnLocation.toMap());
        } catch (IOException e) {
            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-error")));
            throw new RuntimeException(e);
        }

        ConfigUtils.t("function.spawn.create-success", player).thenAccept(player::sendMessage);
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.C_INSTANCE.getObject(FilePath.SPAWN_CONFIG.name()));
    }

}
