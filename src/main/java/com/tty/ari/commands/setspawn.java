package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        Ari.instance.getConfigurationManager().get(FunctionConfig.class).setValue("spawn.location", spawnLocation.toMap());
        ConfigUtils.t("function.spawn.create-success", player).thenAccept(player::sendMessage);

    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(FunctionConfig.class).isEnable(TeleportType.SPAWN);
    }

}
