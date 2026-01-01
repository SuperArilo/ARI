package com.tty.listener;

import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.lib.Log;
import com.tty.lib.tool.ComponentUtils;
import com.tty.states.PlayerSaveStateService;
import com.tty.states.teleport.RandomTpStateService;
import com.tty.lib.services.StateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPluginReloadListener implements Listener {

    @EventHandler
    public void pluginReload(CustomPluginReloadEvent event) {
        Ari.reloadAllConfig();
        Log.init(Ari.instance.getLogger(), Ari.DEBUG);
        if (Ari.DEBUG) {
            Ari.instance.sqlInstance.reconnect();
        }
        RandomTpStateService.setRtpWorldConfig();
        Ari.instance.stateMachineManager.forEach(StateService::abort);

        //重新添加玩家保存state
        PlayerSaveStateService.addPlayerState();
        event.getSender().sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("function.reload.success")));
    }

}
