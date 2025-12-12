package com.tty.commands;

import com.tty.Ari;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.TeleportType;
import com.tty.lib.Log;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class spawn extends BaseLiteralArgumentLiteralCommand {

    public spawn() {
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

        Location value = Ari.C_INSTANCE.getValue("main.location", FilePath.SPAWN_CONFIG, Location.class);
        if(value == null) {
            Log.debug("location null");
            player.sendMessage(ConfigUtils.t("function.spawn.no-spawn"));
            return;
        }
        Ari.instance.stateMachineManager
                .get(TeleportStateService.class)
                .addState(new EntityToLocationState(
                        player,
                        Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.SPAWN_CONFIG, Integer.class, 3),
                        value,
                        TeleportType.SPAWN));
    }

    @Override
    public String name() {
        return "spawn";
    }

    @Override
    public String permission() {
        return "ari.command.spawn";
    }
}
