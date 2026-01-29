package com.tty.commands;

import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "maintenance", permission = "ari.command.maintenance", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class maintenance extends BaseLiteralArgumentLiteralCommand {

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
            ConfigUtils.t("server.maintenance.to-player", player).thenAccept(player::sendMessage);
            Lib.Scheduler.runAtEntityLater(
                    Ari.instance,
                    player,
                    i -> {
                        if (!player.isOnline()) return;
                        player.kick(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
                    },
                    () -> {},
                    this.getMaintenanceKickDelay() * 20L);
        }
        if(!(sender instanceof Player player)) {
            sender.sendMessage(component);
        }
    }

    private int getMaintenanceKickDelay() {
        return Ari.instance.getConfig().getInt("server.maintenance.kick_delay", 10);
    }
}
