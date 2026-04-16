package com.tty.listener.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.tty.Ari;
import com.tty.dto.BungeeCache;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GetServerListListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("GetServers")) {
            String serverList = in.readUTF();
            Set<String> servers = Set.of(serverList.split(",\\s*"));
            Ari.instance.getLog().debug("Received server list from BungeeCord: " + servers);
            BungeeCache.onServersLoaded(servers);
        }

    }

}
