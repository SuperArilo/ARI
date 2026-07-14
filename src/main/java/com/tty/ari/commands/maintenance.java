package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.player.MaintenanceBossBarState;
import com.tty.ari.states.MaintenanceBossBarService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandMeta(displayName = "maintenance", permission = "ari.command.maintenance", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class maintenance extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MaintenanceBossBarService service = Ari.instance.getStatusManager().get(MaintenanceBossBarService.class);
        service.setMaintenance(!service.isMaintenance());

        Component component = ConfigUtils.tAfter("server.maintenance." + (service.isMaintenance() ? "on-enable" : "on-disable"));
        for (Player player : new ArrayList<>(Bukkit.getServer().getOnlinePlayers())) {
            if (player.isOp()) {
                player.sendMessage(component);
                if (service.isMaintenance()) {
                    service.addState(new MaintenanceBossBarState(player));
                } else {
                    service.stopStateByOwner(player);
                }
                continue;
            }
            if (service.isMaintenance()) {
                ConfigUtils.t("server.maintenance.to-player", player).thenAccept(player::sendMessage);
                Ari.instance.getScheduler().runAtEntityLater(
                        player,
                        i -> {
                            if (!player.isOnline()) return;
                            player.kick(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
                        },
                        () -> {},
                        this.getMaintenanceKickDelay() * 20L);
            }
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage(component);
        }
    }

    private int getMaintenanceKickDelay() {
        return Ari.instance.getConfig().getInt("server.maintenance.kick_delay", 10);
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
