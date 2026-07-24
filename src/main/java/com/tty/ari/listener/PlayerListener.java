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

    @EventHandler(priority = EventPriority.HIGH)
    public void spam(PlayerKickEvent event) {
        PlayerKickEvent.Cause cause = event.getCause();
        if (cause != PlayerKickEvent.Cause.SPAM && cause != PlayerKickEvent.Cause.TOO_MANY_PENDING_CHATS) return;

        Player player = event.getPlayer();
        boolean canPass = player.isOp() || Ari.PERMISSION_SERVICE.hasPermission(player, "ari.pass-spam");
        event.setCancelled(canPass);
    }

}
