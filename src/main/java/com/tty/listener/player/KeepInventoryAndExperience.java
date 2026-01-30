package com.tty.listener.player;

import com.tty.Ari;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KeepInventoryAndExperience implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = Ari.instance.getConfig();
        if (Ari.PERMISSION_SERVICE.hasPermission(player, "ari.keep-inventory") && config.getBoolean("server.enable-keep-inventory", false)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }
        if (Ari.PERMISSION_SERVICE.hasPermission(player, "ari.keep-experience") && config.getBoolean("server.enable-keep-experience", false)) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
    }
}
