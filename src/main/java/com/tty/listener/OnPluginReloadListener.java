package com.tty.listener;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.lib.Log;
import com.tty.lib.tool.ComponentUtils;
import com.tty.states.PlayerSaveStateService;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.lib.services.StateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OnPluginReloadListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void pluginReload(CustomPluginReloadEvent event) {
        Ari.reloadAllConfig();
        Log.init(Ari.instance.getLogger(), Ari.DEBUG);
        if (Ari.DEBUG) {
            Ari.SQL_INSTANCE.reconnect();
        }
        RandomTpStateService.setRtpWorldConfig();
        Ari.REPOSITORY_MANAGER.clearAllCache();
        Ari.STATE_MACHINE_MANAGER.forEach(StateService::abort);

        //重新添加玩家保存state
        PlayerSaveStateService.addPlayerState();
        event.getSender().sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("function.reload.success")));
    }

}
