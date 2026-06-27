package com.tty.ari.listener.teleport;

import com.google.common.reflect.TypeToken;
import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.api.event.WhenPluginConfigUpdateEvent;
import com.tty.ari.Ari;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.dto.event.CustomPlayerRespawnEvent;
import com.tty.api.ServerPlatform;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class RecordLastLocationListener implements Listener {

    //保存的玩家上一个传送位置
    public static final Map<UUID, Location> TELEPORT_LAST_LOCATION = new ConcurrentHashMap<>();

    private SpawnLocation spawnLocation;

    @EventHandler
    public void onRespawnOnFolia(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getType() != InventoryType.CRAFTING || !player.isDead() || !player.isConnected() || player.getHealth() > 0) return;
        // do stuff
        if (!ServerPlatform.isFolia()) return;
        Ari.instance.getScheduler().runAtEntity(Ari.instance, player, i -> {
            Location respawnLocation = player.getRespawnLocation();
            if (respawnLocation == null) {
                Location location = this.getRespawnLocation(player.getWorld());
                player.setRespawnLocation(location);
                respawnLocation = location;
            }
            Bukkit.getPluginManager().callEvent(new CustomPlayerRespawnEvent(player, respawnLocation, player.getLocation()));
        }, null);
    }

    @EventHandler
    public void lastLocation(PlayerTeleportEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!cause.equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) return;
        TELEPORT_LAST_LOCATION.put(event.getPlayer().getUniqueId(), event.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void lastDeathLocation(PlayerDeathEvent event) {
        TELEPORT_LAST_LOCATION.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onRespawnOnFolia(CustomPlayerRespawnEvent event) {
        if (!ServerPlatform.isFolia()) return;
        Player player = event.getPlayer();
        Location respawnLocation = event.getRespawnLocation();
        Ari.instance.getScheduler().runAtRegion(Ari.instance, respawnLocation, i -> player.teleportAsync(respawnLocation).thenAccept(t -> {
            if(t && TELEPORT_LAST_LOCATION.containsKey(player.getUniqueId())) {
                this.setPlayerLastLocation(player);
            } else {
                Ari.instance.getLog().warn("player {} teleport status error. location: {}", player.getName(), respawnLocation.toString());
            }
        }));
    }

    @EventHandler
    public void onRespawnOnPaper(PlayerRespawnEvent event) {
        if (event.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.END_PORTAL)) return;
        if (ServerPlatform.isFolia()) return;
        Player player = event.getPlayer();
        if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            event.setRespawnLocation(getRespawnLocation(player.getWorld()));
        }

        if (TELEPORT_LAST_LOCATION.containsKey(player.getUniqueId())) {
            this.setPlayerLastLocation(player);
        }
    }

    @EventHandler
    public void cleanPlayerLastLocation(PlayerQuitEvent event) {
        TELEPORT_LAST_LOCATION.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onReload(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        this.spawnLocation = this.getSpawnLocation();
    }

    @EventHandler
    public void onConfigIUpdate(WhenPluginConfigUpdateEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        this.spawnLocation = this.getSpawnLocation();
    }

    private void setPlayerLastLocation(Player player) {
        ConfigUtils.t("teleport.tips-back", player).thenAccept(i ->
                Ari.instance.getScheduler().runAtEntity(Ari.instance, player, t ->
                        player.sendMessage(Ari.instance.getComponentTool().setClickEventText(i, ClickEvent.Action.RUN_COMMAND, "/" + Ari.instance.getName() + " back")), null));
    }

    public Location getRespawnLocation(@NotNull World world) {
        Location location;
        if (this.spawnLocation != null) {
            location = new Location(Bukkit.getServer().getWorld(this.spawnLocation.getWorldName()), this.spawnLocation.getX(), this.spawnLocation.getY(), this.spawnLocation.getZ(), this.spawnLocation.getYaw(), this.spawnLocation.getPitch());
        } else {
            Ari.instance.getLog().debug("not setting spawn location.fallback server spawn location.");
            location = world.getSpawnLocation();
        }
        return location;
    }

    private SpawnLocation getSpawnLocation() {
        return Ari.instance.getConfigInstance().getValue("spawn.location", FilePath.FUNCTION_CONFIG, new TypeToken<SpawnLocation>(){}.getType(), null);
    }

}
