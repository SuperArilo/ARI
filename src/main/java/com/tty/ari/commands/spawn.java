package com.tty.ari.commands;

import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.TeleportType;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
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

    private final TypeToken<SpawnLocation> typeToken = new TypeToken<>(){};

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        SpawnLocation value = Ari.instance.getConfigInstance().getValue("spawn.location", FilePath.FUNCTION_CONFIG, typeToken.getType(), null);
        if(value == null) {
            ConfigUtils.t("function.spawn.no-spawn", player).thenAccept(player::sendMessage);
            return 0;
        }
        Ari.STATE_MACHINE_MANAGER
            .get(TeleportStateService.class)
            .addState(new EntityToLocationState(
                    player,
                    Ari.instance.getConfigInstance().getValue("spawn.teleport.delay", FilePath.FUNCTION_CONFIG, Integer.class, 3),
                    new Location(Bukkit.getWorld(value.getWorldName()), value.getX(), value.getY(), value.getZ(), value.getYaw(), value.getPitch()),
                    TeleportType.SPAWN));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigInstance().getValue("spawn.enable", FilePath.FUNCTION_CONFIG, Boolean.class, true);
    }
}
