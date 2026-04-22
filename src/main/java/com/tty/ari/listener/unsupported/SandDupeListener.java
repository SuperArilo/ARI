package com.tty.ari.listener.unsupported;

import com.tty.ari.Ari;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.util.Vector;


public class SandDupeListener implements Listener {

    private final boolean enable;
    private final String overworld;
    private final String endWorld;

    public SandDupeListener() {
        this.enable = this.getEnable();
        this.overworld = this.getOverworld();
        this.endWorld = this.getEndWorld();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalEnterEvent(EntityPortalEnterEvent event) {

        if (!this.enable) return;
        if (event.getLocation().getBlock().getType() != Material.END_PORTAL) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof FallingBlock fallingBlock)) return;

        BlockData blockData = fallingBlock.getBlockData();
        Vector velocity = fallingBlock.getVelocity();

        World sourceWorld = event.getLocation().getWorld();
        World.Environment sourceEnv = sourceWorld.getEnvironment();

        // 从末地返回主世界
        if (sourceEnv == World.Environment.THE_END) {
            World overworld = Bukkit.getWorld(this.overworld);
            if (overworld == null) return;

            int chunkX = 0;
            int chunkZ = 0;
            Ari.instance.getScheduler().runAtRegion(Ari.instance, overworld, chunkX, chunkZ, task -> {
                Location safeLocation = this.findSafeLocation(overworld);
                if (safeLocation == null) {
                    Ari.instance.getLog().warn("No safe block found in overworld");
                    return;
                }
                overworld.spawn(safeLocation, FallingBlock.class, CreatureSpawnEvent.SpawnReason.CUSTOM, i  -> i.setBlockData(blockData))
                        .setVelocity(velocity);
            });
        }
        // 从主世界进入末地
        else if (sourceEnv == World.Environment.NORMAL) {
            World endWorld = Bukkit.getWorld(this.endWorld);
            if (endWorld == null) return;

            Location location = new Location(endWorld, 100.5, 50, 0.5);
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;

            Ari.instance.getScheduler().runAtRegion(Ari.instance,
                    endWorld,
                    chunkX,
                    chunkZ,
                    task ->
                            endWorld.spawn(location, FallingBlock.class, CreatureSpawnEvent.SpawnReason.CUSTOM, i -> i.setBlockData(blockData)).setVelocity(velocity));
        }
    }

    private Location findSafeLocation(World world) {
        double y = world.getMaxHeight() - 1;
        while (y >= world.getMinHeight()) {
            Block block = new Location(world, 0, y, 0).getBlock();
            if (block.getType() != Material.AIR && block.getType() != Material.VOID_AIR && block.getType() != Material.CAVE_AIR) {
                return new Location(world, 0, y + 1, 0);
            }
            y--;
        }
        return null;
    }

    private boolean getEnable() {
        return Ari.instance.getConfig().getBoolean("server.unsupported-settings.sand-dupe.enable", false);
    }

    private String getOverworld() {
        return Ari.instance.getConfig().getString("server.unsupported-settings.sand-dupe.overworld", "world");
    }

    private String getEndWorld() {
        return Ari.instance.getConfig().getString("server.unsupported-settings.sand-dupe.end", "world_the_end");
    }

}