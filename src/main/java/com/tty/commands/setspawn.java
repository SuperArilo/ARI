package com.tty.commands;

import com.tty.Ari;
import com.tty.dto.SpawnLocation;
import com.tty.enumType.FilePath;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "setspawn", permission = "ari.command.setspawn", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class setspawn extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.SPAWN_CONFIG.name()))) return;

        Player player = (Player) sender;
        Location location = player.getLocation();
        SpawnLocation spawnLocation = new SpawnLocation();
        spawnLocation.setWorldName(player.getWorld().getName());
        spawnLocation.setX(location.getX());
        spawnLocation.setY(location.getY());
        spawnLocation.setZ(location.getZ());
        spawnLocation.setPitch(location.getPitch());
        spawnLocation.setYaw(location.getYaw());

        Ari.C_INSTANCE.setValue(Ari.instance, "main.location", FilePath.SPAWN_CONFIG, spawnLocation.toMap());

        ConfigUtils.t("function.spawn.create-success", player).thenAccept(player::sendMessage);
    }

}
