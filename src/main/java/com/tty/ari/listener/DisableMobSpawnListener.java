package com.tty.ari.listener;

import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.ari.Ari;
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

    @EventHandler
    public void disableSpawn(EntitySpawnEvent event) {
        if (this.isDisabled) return;
        EntityType entityType = event.getEntityType();
        if (this.disableList.contains(entityType)) {
            Entity entity = event.getEntity();
            event.setCancelled(true);
            entity.remove();
        }
    }

    @EventHandler
    public void reload(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        this.isDisabled = this.loadDisabled();
        this.disableList = this.loadDisableList();
    }

    private boolean loadDisabled() {
        return !Ari.instance.getConfig().getBoolean("server.anti-entity-spawn.enable");
    }

    private List<EntityType> loadDisableList() {
        List<String> list = Ari.instance.getConfig().getStringList("server.anti-entity-spawn.list");
        return this.convertStringListToEnumList(list, EntityType.class, false);
    }

    public <T extends Enum<T>> List<T> convertStringListToEnumList(List<String> stringList, Class<T> enumClass, boolean caseSensitive) throws IllegalArgumentException {
        List<T> result = new ArrayList<>();
        for (String item : stringList) {
            if (item == null || item.trim().isEmpty()) {
                continue;
            }
            String cleanName = item.trim();
            String enumName = caseSensitive ? cleanName.toLowerCase() : cleanName.toUpperCase();
            T enumValue = Enum.valueOf(enumClass, enumName);
            result.add(enumValue);
        }
        return result;
    }

}
