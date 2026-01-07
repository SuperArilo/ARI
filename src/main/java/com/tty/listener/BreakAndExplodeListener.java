package com.tty.listener;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.tool.FireworkUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BreakAndExplodeListener implements Listener {

    private final FireworkUtils utils = new FireworkUtils(Ari.instance);

    private List<String> passExplosionList;
    private boolean antiExplosion;
    private boolean antiTrampleFarmland;
    private boolean antiFireSpread;

    public BreakAndExplodeListener() {
        this.antiExplosion = this.loadAntiExplosion();
        this.passExplosionList = this.loadPassExplosionList();
        this.antiTrampleFarmland = this.loadAntiTrampleFarmland();
        this.antiFireSpread = this.loadAntiFireSpread();
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!this.antiExplosion) return;

        EntityType entityType = event.getEntityType();
        if (this.passExplosionList.contains(entityType.name())) return;
        event.blockList().clear();
        this.utils.spawnFireworks(event.getLocation(), 1);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!this.antiExplosion) return;
        Material material = event.getBlock().getType();
        if (material == Material.AIR) return;

        if (this.passExplosionList.contains(material.name())) return;

        event.blockList().clear();
        this.utils.spawnFireworks(
                event.getBlock().getLocation().add(0.5, 2, 0.5),
                1
        );
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!this.antiExplosion) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Material type = block.getType();
        boolean isBed = Tag.BEDS.isTagged(type);
        if (isBed) {
            if (this.passExplosionList.stream().anyMatch(i -> i.endsWith("BED"))) return;
        }
        if (this.passExplosionList.contains(type.name())) return;
        if (type == Material.RESPAWN_ANCHOR) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof RespawnAnchor anchor) {
                ItemStack item = event.getItem();
                if (item != null && item.getType() == Material.GLOWSTONE) {
                    if (anchor.getCharges() >= anchor.getMaximumCharges()) {
                        event.setCancelled(true);
                    }
                }
            }
        } else if (isBed) {
            World world = block.getWorld();
            if (world.getEnvironment() != World.Environment.NORMAL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!this.antiTrampleFarmland) return;
        if (event.getBlock().getType() == Material.FARMLAND && event.getTo() == Material.DIRT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void fireSpread(BlockSpreadEvent event) {
        if (!this.antiFireSpread) return;
        if (event.getSource().getType() == Material.FIRE || event.getBlock().getType() == Material.FIRE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onReload(CustomPluginReloadEvent event) {
        this.passExplosionList = this.loadPassExplosionList();
        this.antiExplosion = this.loadAntiExplosion();
        this.antiTrampleFarmland = this.loadAntiTrampleFarmland();
        this.antiFireSpread = this.loadAntiFireSpread();
    }

    private List<String> loadPassExplosionList() {
        List<String> result = new ArrayList<>();
        List<String> list = Ari.instance.getConfig().getStringList("server.anti-explosion.pass-list");
        for (String s : list) {
            result.add(s.toUpperCase());

        }
        return result;
    }

    private boolean loadAntiTrampleFarmland() {
        return Ari.instance.getConfig().getBoolean("server.anti-trample-farmland", false);
    }

    private boolean loadAntiExplosion() {
        return Ari.C_INSTANCE.getValue(
                "anti-explosion.enable",
                FilePath.FUNCTION_CONFIG,
                Boolean.class,
                false
        );
    }

    private boolean loadAntiFireSpread() {
        return Ari.instance.getConfig().getBoolean("server.anti-fire-spread");
    }
}
