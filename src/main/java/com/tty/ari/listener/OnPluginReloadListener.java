package com.tty.ari.listener;

import com.tty.api.ComponentTool;
import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.ari.Ari;
import com.tty.ari.configuration.lang.LangConfig;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPluginReloadListener implements Listener {

    @EventHandler
    public void onConfigReloaded(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        CommandSender sender = event.getSender();
        if (sender != null) {
            if (Ari.instance.isDebug()) {
                Ari.SQL_INSTANCE.reconnect();
            }
            Ari.REPOSITORY_MANAGER.clearAllCache();
            Ari.instance.getStatusManager().reload();
            Ari.PLACEHOLDER.setInstance(Ari.instance.getConfigurationManager().get(LangConfig.class));
            Ari.BUNGEECACHE.shutdown();
            PlayerCache.clean();
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.reload.success")));
        }
    }
}
