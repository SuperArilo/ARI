package com.tty.listener;

import com.tty.Ari;
import com.tty.commands.infinitytotem;
import com.tty.dto.SpawnLocation;
import com.tty.dto.event.CustomPlayerRespawnEvent;
import com.tty.enumType.FilePath;
import com.tty.api.Log;
import com.tty.api.ServerPlatform;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    @EventHandler
    public void onRespawn(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getType() != InventoryType.CRAFTING || !player.isDead() || !player.isConnected() || player.getHealth() > 0) return;
        // do stuff
        if (!ServerPlatform.isFolia()) return;
        Ari.SCHEDULER.runAtEntity(Ari.instance, player,i -> {
            Location respawnLocation = ((Player) event.getPlayer()).getRespawnLocation();
            if (respawnLocation == null) {
                respawnLocation = getRespawnLocation(player.getWorld());
            }
            Bukkit.getPluginManager().callEvent(new CustomPlayerRespawnEvent(player, respawnLocation, player.getLocation()));
        }, null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExit(PlayerQuitEvent event) {
        infinitytotem.INFINITY_TOTEM_PLAYER_LIST.remove(event.getPlayer());
    }

    public static Location getRespawnLocation(@NotNull World world) {
        Location location;
        SpawnLocation value = Ari.C_INSTANCE.getValue("main.location", FilePath.SPAWN_CONFIG, SpawnLocation.class, null);
        if (value != null) {
            location = new Location(Bukkit.getWorld(value.getWorldName()), value.getX(), value.getY(), value.getZ(), value.getYaw(), value.getPitch());
        } else {
            Log.debug("not setting spawn location.fallback server spawn location.");
            location = world.getSpawnLocation();
        }
        return location;
    }

}
