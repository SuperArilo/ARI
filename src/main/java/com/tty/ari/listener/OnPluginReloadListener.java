package com.tty.ari.listener;

import com.tty.api.event.WhenPluginExecuteReloadCommandEvent;
import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPluginReloadListener implements Listener {

    @EventHandler
    public void pluginReload(WhenPluginExecuteReloadCommandEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        Ari.instance.doReloadAllFiles(event.getSender());
    }


    @EventHandler
    public void onConfigReloaded(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        CommandSender sender = event.getSender();
        if (sender != null) {
            if (Ari.instance.isDebug()) {
                Ari.SQL_INSTANCE.reconnect();
            }
            Ari.REPOSITORY_MANAGER.clearAllCache();
            Ari.STATE_MACHINE_MANAGER.forEach(StateService::onReload);
            Ari.PLACEHOLDER.setInstance(Ari.instance.getConfigInstance());
            Ari.BUNGEECACHE.shutdown();
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("function.reload.success")));
        }
    }
}
