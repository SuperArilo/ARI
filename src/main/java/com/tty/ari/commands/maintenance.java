package com.tty.ari.commands;

import com.tty.api.ComponentTool;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.scheduler.RunTask;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.player.MaintenanceBossBarState;
import com.tty.ari.states.MaintenanceBossBarService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CommandMeta(displayName = "maintenance", permission = "ari.command.maintenance", tokenLength = 1, allowConsole = true)
@LiteralCommand(directExecute = true)
public class maintenance extends LiteralArgumentCommand {

    private final Map<Player, RunTask> playerTaskMap = new ConcurrentHashMap<>();

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MaintenanceBossBarService service = Ari.instance.getStatusManager().get(MaintenanceBossBarService.class);
        service.setMaintenance(!service.isMaintenance());

        Component enable = ConfigUtils.tAfter("server.maintenance.on-enable");
        Component disable = ConfigUtils.tAfter("server.maintenance.on-disable");

        boolean isMaintenance = service.isMaintenance();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(isMaintenance ? enable : disable);
                if (isMaintenance) {
                    service.addState(new MaintenanceBossBarState(player));
                } else {
                    service.stopStateByOwner(player);
                }
            } else {
                if (isMaintenance) {
                    ConfigUtils.t("server.maintenance.to-player", player).thenAccept(player::sendMessage);
                    RunTask old = this.playerTaskMap.remove(player);
                    if (old != null) old.cancel();

                    RunTask runTask = Ari.instance.getScheduler().runAtEntityLater(player, i -> {
                        try {
                            if (!player.isOnline() || !service.isMaintenance() || player.isOp()) return;
                            player.kick(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
                        } finally {
                            this.playerTaskMap.remove(player);
                        }
                    }, null, getMaintenanceKickDelay() * 20L);
                    this.playerTaskMap.put(player, runTask);
                } else {
                    player.sendMessage(disable);
                }
            }
        }

        if (!isMaintenance) {
            for (RunTask task : this.playerTaskMap.values()) {
                if (task != null) task.cancel();
            }
            this.playerTaskMap.clear();
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(isMaintenance ? enable : disable);
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