package com.tty.listener.teleport;

import com.tty.Ari;
import com.tty.dto.event.CustomPlayerRespawnEvent;
import com.tty.lib.Lib;
import com.tty.lib.ServerPlatform;
import com.tty.lib.tool.ComponentUtils;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.tty.listener.PlayerListener.getRespawnLocation;

public class RecordLastLocationListener implements Listener {

    //保存的玩家上一个传送位置
    public static final Map<UUID, Location> TELEPORT_LAST_LOCATION = new ConcurrentHashMap<>();

    @EventHandler
    public void lastLocation(PlayerTeleportEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!cause.equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) return;
        TELEPORT_LAST_LOCATION.put(event.getPlayer().getUniqueId(), event.getFrom());
    }
    @EventHandler
    public void lastDeathLocation(PlayerDeathEvent event) {
        TELEPORT_LAST_LOCATION.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
    }
    @EventHandler
    public void onRespawn(CustomPlayerRespawnEvent event) {
        if (!ServerPlatform.isFolia()) return;
        Player player = event.getPlayer();
        Location respawnLocation = event.getRespawnLocation();
        player.setRespawnLocation(respawnLocation);
        Lib.Scheduler.runAtRegion(
            Ari.instance,
            respawnLocation,
            i -> player.teleportAsync(respawnLocation).thenAccept(t -> this.setPlayerLastLocation(event))
        );

    }
    @EventHandler
    public void onRespawnOnPaper(PlayerRespawnEvent event) {
        if (ServerPlatform.isFolia()) return;
        Player player = event.getPlayer();
        if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            event.setRespawnLocation(getRespawnLocation(player.getWorld()));
        }
        this.setPlayerLastLocation(event);
    }
    @EventHandler
    public void cleanPlayerLastLocation(PlayerQuitEvent event) {
        TELEPORT_LAST_LOCATION.remove(event.getPlayer().getUniqueId());
    }

    private void setPlayerLastLocation(PlayerEvent event) {
        Player player = event.getPlayer();
        Ari.PLACEHOLDER.renderAsync("teleport.tips-back", player).thenAccept(i -> player.sendMessage(ComponentUtils.setClickEventText(i, ClickEvent.Action.RUN_COMMAND, "/back")));
    }
}
