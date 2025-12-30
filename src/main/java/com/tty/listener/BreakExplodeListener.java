package com.tty.listener;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.tool.FireworkUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BreakExplodeListener implements Listener {

    private final FireworkUtils utils = new FireworkUtils(Ari.instance);
    private Set<String> passSet;

    public BreakExplodeListener() {
        this.reloadPassSet();
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (this.isDisabled()) return;

        EntityType entityType = event.getEntityType();

        if (this.passSet.contains(entityType.name())) {
            return;
        }

        event.blockList().clear();
        this.utils.spawnFireworks(event.getLocation(), 1);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (this.isDisabled()) return;

        Material material = event.getBlock().getType();

        if (this.passSet.contains(material.name())) {
            return;
        }

        event.blockList().clear();
        this.utils.spawnFireworks(
                event.getBlock().getLocation().add(0.5, 2, 0.5),
                1
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onReload(CustomPluginReloadEvent event) {
        this.reloadPassSet();
    }

    private void reloadPassSet() {
        this.passSet = passList().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    private boolean isDisabled() {
        return !Ari.C_INSTANCE.getValue(
                "anti-explosion.enable",
                FilePath.FUNCTION_CONFIG,
                Boolean.class,
                false
        );
    }

    private List<String> passList() {
        return Ari.C_INSTANCE.getValue(
                "anti-explosion.pass-list",
                FilePath.FUNCTION_CONFIG,
                new TypeToken<List<String>>() {}.getType(),
                List.of()
        );
    }
}
