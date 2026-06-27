package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.player.MaintenanceBossBarState;
import com.tty.ari.states.MaintenanceBossBarService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
    public int execute(CommandSender sender, String[] args) {
        MAINTENANCE_MODE = !MAINTENANCE_MODE;
        MaintenanceBossBarService service = Ari.STATE_MACHINE_MANAGER.get(MaintenanceBossBarService.class);

        Component component = ConfigUtils.tAfter("server.maintenance." + (MAINTENANCE_MODE ? "on-enable" : "on-disable"));
        for (Player player : new ArrayList<>(Bukkit.getServer().getOnlinePlayers())) {
            if (player.isOp()) {
                player.sendMessage(component);
                if (MAINTENANCE_MODE) {
                    service.addState(new MaintenanceBossBarState(player, component, 1.0f, BossBar.Color.BLUE, Integer.MAX_VALUE));
                } else {
                    service.stopStateByOwner(player);
                }
                continue;
            }
            if (MAINTENANCE_MODE) {
                ConfigUtils.t("server.maintenance.to-player", player).thenAccept(player::sendMessage);
                Ari.instance.getScheduler().runAtEntityLater(
                        Ari.instance,
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
        return Command.SINGLE_SUCCESS;
    }

    private int getMaintenanceKickDelay() {
        return Ari.instance.getConfig().getInt("server.maintenance.kick_delay", 10);
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
