package com.tty.commands;

import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.dto.SpawnLocation;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.enumType.FilePath;
import com.tty.enumType.TeleportType;
import com.tty.api.Log;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "spawn", permission = "ari.command.spawn", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class spawn extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        SpawnLocation value = Ari.C_INSTANCE.getValue("main.location", FilePath.SPAWN_CONFIG, SpawnLocation.class, null);
        if(value == null) {
            Log.debug("location null");
            ConfigUtils.t("function.spawn.no-spawn", player).thenAccept(player::sendMessage);
            return;
        }
        Ari.STATE_MACHINE_MANAGER
            .get(TeleportStateService.class)
            .addState(new EntityToLocationState(
                    player,
                    Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.SPAWN_CONFIG, Integer.class, 3),
                    new Location(Bukkit.getWorld(value.getWorldName()), value.getX(), value.getY(), value.getZ(), value.getYaw(), value.getPitch()),
                    TeleportType.SPAWN));
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.C_INSTANCE.getObject(FilePath.SPAWN_CONFIG.name()));
    }
}
