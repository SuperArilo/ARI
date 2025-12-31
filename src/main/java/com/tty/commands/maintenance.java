package com.tty.commands;

import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class maintenance extends BaseLiteralArgumentLiteralCommand {

    public static boolean MAINTENANCE_MODE = false;

    public maintenance() {
        super(true, 1, true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "maintenance";
    }

    @Override
    public String permission() {
        return "ari.command.maintenance";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MAINTENANCE_MODE = !MAINTENANCE_MODE;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) continue;
            player.sendMessage(ConfigUtils.t(
                    "server.maintenance.to-player",
                    Map.of(LangType.MAINTENANCE_KICK_DEALY.getType(), Component.text(this.getMaintenanceKickDelay()))));
            Lib.Scheduler.runAtEntityLater(
                    Ari.instance,
                    player,
                    i -> {
                        if (!player.isOnline()) return;
                        player.kick(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-player.data-changed")));
                    },
                    () -> {},
                    this.getMaintenanceKickDelay() * 20L);
        }
        sender.sendMessage(ConfigUtils.t("server.maintenance." + (MAINTENANCE_MODE ? "on-enable":"on-disable")));
    }

    private int getMaintenanceKickDelay() {
        return Ari.instance.getConfig().getInt("server.maintenance.kick_delay", 10);
    }
}
