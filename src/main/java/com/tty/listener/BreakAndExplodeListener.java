package com.tty.listener;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.Log;
import com.tty.lib.tool.FireworkUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BreakAndExplodeListener implements Listener {

    private final FireworkUtils utils = new FireworkUtils(Ari.instance);
    private Set<String> passSet;
    private final Set<EntityType> supported_entity_explosions = Set.of(
            EntityType.TNT,        // TNT 实体爆炸
            EntityType.TNT_MINECART,      // TNT 矿车爆炸
            EntityType.CREEPER,           // 苦力怕爆炸
            EntityType.WITHER,            // 凋灵爆炸
            EntityType.WITHER_SKULL,      // 凋灵头颅爆炸
            EntityType.END_CRYSTAL,     // 末影水晶爆炸
            EntityType.FIREBALL,          // Ghast / Blaze 火球爆炸
            EntityType.SMALL_FIREBALL,     // 小火球效果爆炸
            EntityType.WIND_CHARGE // 风弹
    );

    public BreakAndExplodeListener() {
        this.reloadPassSet();
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (this.isDisabled()) return;

        EntityType entityType = event.getEntityType();

        if (!this.supported_entity_explosions.contains(entityType)) return;

        if (this.passSet.contains(entityType.name())) return;

        event.blockList().clear();
        this.utils.spawnFireworks(event.getLocation(), 1);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (this.isDisabled()) return;

        Material material = event.getBlock().getType();
        if (material == Material.AIR) return;

        if (this.passSet.contains(material.name())) return;

        event.blockList().clear();
        this.utils.spawnFireworks(
                event.getBlock().getLocation().add(0.5, 2, 0.5),
                1
        );
    }

    @EventHandler
    public void onPlayerStep(PlayerInteractEvent event) {
        if (!Ari.instance.getConfig().getBoolean("server.anti-trample-farmland", false)) return;
        if (event.getAction() != Action.PHYSICAL) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.FARMLAND) return;
        Log.debug("player %s try break farmland.", event.getPlayer().getName());
        event.setCancelled(true);
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
