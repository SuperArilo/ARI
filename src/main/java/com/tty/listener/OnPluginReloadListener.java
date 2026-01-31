package com.tty.listener;

import com.tty.Ari;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.states.PlayerSaveStateService;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.api.state.StateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OnPluginReloadListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void pluginReload(CustomPluginReloadEvent event) {
        Ari.reloadAllConfig();
        Ari.LOG.setDebug(Ari.DEBUG);
        if (Ari.DEBUG) {
            Ari.SQL_INSTANCE.reconnect();
        }
        RandomTpStateService.setRtpWorldConfig();
        Ari.REPOSITORY_MANAGER.clearAllCache();
        Ari.STATE_MACHINE_MANAGER.forEach(StateService::abort);
        //重新添加玩家保存state
        PlayerSaveStateService.addPlayerState();

        Ari.setDebugStatus();
    }

}
