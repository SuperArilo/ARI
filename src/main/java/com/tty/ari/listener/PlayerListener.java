package com.tty.ari.listener;

import com.tty.ari.Ari;
import com.tty.ari.commands.infinitytotem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExit(PlayerQuitEvent event) {
        infinitytotem.INFINITY_TOTEM_PLAYER_LIST.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void spam(PlayerKickEvent event) {
        if (!event.getCause().equals(PlayerKickEvent.Cause.SPAM)) return;
        Player player = event.getPlayer();
        if (player.isOp() || Ari.PERMISSION_SERVICE.hasPermission(player, "ari.pass-spam")) {
            event.setCancelled(true);
        }
    }

}
