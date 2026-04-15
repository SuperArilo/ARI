package com.tty.listener;

import com.tty.Ari;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.dto.BungeeCache;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.api.state.StateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;

public class OnPluginReloadListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void pluginReload(CustomPluginReloadEvent event) {
        Ari.reloadAllConfig();
        if (Ari.DEBUG) {
            Ari.SQL_INSTANCE.reconnect();
        }
        RandomTpStateService.setRtpWorldConfig();
        Ari.REPOSITORY_MANAGER.clearAllCache();
        Ari.STATE_MACHINE_MANAGER.forEach(StateService::reload);
        BungeeCache.setServers(Set.of());
        BungeeCache.setState(BungeeCache.State.UNKNOWN);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void debugStatus(CustomPluginReloadEvent event) {
        Ari.REPOSITORY_MANAGER.debug(Ari.DEBUG);
        Ari.LOG.setDebug(Ari.DEBUG);
    }

}
