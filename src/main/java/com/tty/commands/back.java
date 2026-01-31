package com.tty.commands;

import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.enumType.FilePath;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.enumType.TeleportType;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.tty.listener.teleport.RecordLastLocationListener.TELEPORT_LAST_LOCATION;

@CommandMeta(displayName = "back", permission = "ari.command.back", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class back extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        Location beforeLocation = TELEPORT_LAST_LOCATION.get(player.getUniqueId());
        if(beforeLocation == null) {
            ConfigUtils.t("teleport.none-location", player).thenAccept(player::sendMessage);
            return;
        }

        Ari.STATE_MACHINE_MANAGER
                .get(TeleportStateService.class)
                .addState(new EntityToLocationState(
                        player,
                        Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.BACK_CONFIG, Integer.class, 3),
                        beforeLocation,
                        TeleportType.BACK));
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.C_INSTANCE.getObject(FilePath.BACK_CONFIG.name()));
    }
}
