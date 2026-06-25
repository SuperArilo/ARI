package com.tty.ari.listener;

import com.tty.api.event.OnPluginConfigReloadedEvent;
import com.tty.ari.Ari;
import com.tty.ari.commands.infinitytotem;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.dto.event.CustomPlayerRespawnEvent;
import com.tty.ari.enumType.FilePath;
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

    private static SpawnLocation SPAWN_LOCATION;

    public PlayerListener() {
        SPAWN_LOCATION = this.getSpawnLocation();
    }

    @EventHandler
    public void onRespawnOnFolia(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getType() != InventoryType.CRAFTING || !player.isDead() || !player.isConnected() || player.getHealth() > 0) return;
        // do stuff
        if (!ServerPlatform.isFolia()) return;
        Ari.instance.getScheduler().runAtEntity(Ari.instance, player, i -> {
            Location respawnLocation = player.getRespawnLocation();
            if (respawnLocation == null) {
                Location location = getRespawnLocation(player.getWorld());
                player.setRespawnLocation(location);
                respawnLocation = location;
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
        if (SPAWN_LOCATION != null) {
            location = new Location(Bukkit.getServer().getWorld(SPAWN_LOCATION.getWorldName()), SPAWN_LOCATION.getX(), SPAWN_LOCATION.getY(), SPAWN_LOCATION.getZ(), SPAWN_LOCATION.getYaw(), SPAWN_LOCATION.getPitch());
        } else {
            Ari.instance.getLog().debug("not setting spawn location.fallback server spawn location.");
            location = world.getSpawnLocation();
        }
        return location;
    }

    private SpawnLocation getSpawnLocation() {
        return Ari.instance.getConfigInstance().getValue("spawn.location", FilePath.FUNCTION_CONFIG, SpawnLocation.class, null);
    }

    @EventHandler
    public void onPluginRelo(OnPluginConfigReloadedEvent event) {
        SPAWN_LOCATION = this.getSpawnLocation();
    }

}
