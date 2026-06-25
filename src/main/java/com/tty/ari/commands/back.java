package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.enumType.FilePath;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.tty.ari.listener.teleport.RecordLastLocationListener.TELEPORT_LAST_LOCATION;

@CommandMeta(displayName = "back", permission = "ari.command.back", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class back extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        Location beforeLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
        if(beforeLocation == null) {
            ConfigUtils.t("teleport.none-location", player).thenAccept(player::sendMessage);
            return 0;
        }

        Ari.STATE_MACHINE_MANAGER
                .get(TeleportStateService.class)
                .addState(new EntityToLocationState(
                        player,
                        Ari.instance.getConfigInstance().getValue("back.teleport.delay", FilePath.FUNCTION_CONFIG, Integer.class, 3),
                        beforeLocation,
                        TeleportType.BACK));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigInstance().getValue("back.enable", FilePath.FUNCTION_CONFIG, Boolean.class, true);
    }
}
