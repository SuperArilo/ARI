package com.tty.ari.listener;

import com.tty.ari.Ari;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.api.state.StateService;
import com.tty.ari.dto.BungeeCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;

public class OnPluginReloadListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void pluginReload(CustomPluginReloadEvent event) {
        Ari.instance.doReloadAllFiles();
        if (Ari.instance.isDebug()) {
            Ari.SQL_INSTANCE.reconnect();
        }
        Ari.REPOSITORY_MANAGER.clearAllCache();
        Ari.STATE_MACHINE_MANAGER.forEach(StateService::onReload);
        Ari.PLACEHOLDER.setInstance(Ari.instance.getConfigInstance());
        BungeeCache.setServers(Set.of());
        BungeeCache.setState(BungeeCache.State.UNKNOWN);
    }

}
