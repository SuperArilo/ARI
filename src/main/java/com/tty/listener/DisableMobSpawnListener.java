package com.tty.listener;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.lib.Log;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.ArrayList;
import java.util.List;

public class DisableMobSpawnListener implements Listener {

    private boolean isDisabled;
    private List<EntityType> disableList = new ArrayList<>();

    public DisableMobSpawnListener() {
        this.isDisabled = this.loadDisabled();
        this.disableList = this.loadDisableList();
    }

    @EventHandler
    public void disableSpawn(EntitySpawnEvent event) {
        if (this.isDisabled) return;
        EntityType entityType = event.getEntityType();
        if (this.disableList.contains(entityType)) {
            Entity entity = event.getEntity();
            Log.debug("disable entity %s spawn.", entity.getName());
            event.setCancelled(true);
            entity.remove();
        }
    }

    @EventHandler
    public void reload(CustomPluginReloadEvent event) {
        this.isDisabled = this.loadDisabled();
        this.disableList = this.loadDisableList();
    }

    private boolean loadDisabled() {
        return !Ari.instance.getConfig().getBoolean("server.anti-entity-spawn.enable");
    }

    private List<EntityType> loadDisableList() {
        List<String> list = Ari.instance.getConfig().getStringList("server.anti-entity-spawn.list");
        return PublicFunctionUtils.convertStringListToEnumList(list, EntityType.class, false);
    }

}
