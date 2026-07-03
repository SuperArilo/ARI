package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
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
    public int execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        FunctionConfig config = Ari.instance.getConfigurationManager().get(FunctionConfig.class);

        SpawnLocation value = config.getSpawnLocation();
        if(value == null) {
            ConfigUtils.t("function.spawn.no-spawn", player).thenAccept(player::sendMessage);
            return 0;
        }
        Ari.instance.getStatusManager().get(TeleportStateService.class).addState(
                new EntityToLocationState(
                    player,
                    config.getTeleportDelay(TeleportType.SPAWN),
                    new Location(Bukkit.getWorld(value.getWorldName()), value.getX(), value.getY(), value.getZ(), value.getYaw(), value.getPitch()),
                    TeleportType.SPAWN)
        );

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(FunctionConfig.class).isEnable(TeleportType.SPAWN);
    }
}
