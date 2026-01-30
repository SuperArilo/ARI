package com.tty.commands;

import com.tty.Ari;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.command.LiteralArgumentCommand;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "maintenance", permission = "ari.command.maintenance", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class maintenance extends LiteralArgumentCommand {

    public static boolean MAINTENANCE_MODE = false;

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MAINTENANCE_MODE = !MAINTENANCE_MODE;
        Component component = ConfigUtils.tAfter("server.maintenance." + (MAINTENANCE_MODE ? "on-enable" : "on-disable"));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(component);
                continue;
            }
            if (MAINTENANCE_MODE) {
                ConfigUtils.t("server.maintenance.to-player", player).thenAccept(player::sendMessage);
                Ari.SCHEDULER.runAtEntityLater(
                        Ari.instance,
                        player,
                        i -> {
                            if (!player.isOnline()) return;
                            player.kick(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
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
}
