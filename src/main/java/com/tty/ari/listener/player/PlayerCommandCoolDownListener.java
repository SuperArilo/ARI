package com.tty.ari.listener.player;

import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerPreCommandState;
import com.tty.ari.states.PlayerCommandPreprocessService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommandCoolDownListener implements Listener {

    private static final List<String> PLUGIN_NAMES = new ArrayList<>();
    static {
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            PLUGIN_NAMES.add(plugin.getName());
        }
        PLUGIN_NAMES.sort((a, b) -> Integer.compare(b.length(), a.length()));
    }

    private int commandCoolDown;
    private boolean isEnable;

    @EventHandler
    public void whenPlayerPreprocess(PlayerCommandPreprocessEvent event) {
        if (!this.isEnable) return;
        Player player = event.getPlayer();
        if (player.isOp() || Ari.PERMISSION_SERVICE.hasPermission(player, "ari.skip-command-cooldown")) return;

        if (!Ari.STATE_MACHINE_MANAGER.get(PlayerCommandPreprocessService.class).addState(new PlayerPreCommandState(player, this.getMainSubCommand(event.getMessage()), this.commandCoolDown))) {
            event.setCancelled(true);
            ConfigUtils.t("server.message.command-cooldown", player).thenAccept(player::sendMessage);
        }
    }

    @EventHandler
    public void reload(WhenPluginConfigReloadCompleteEvent event) {
        if (!event.getPlugin().equals(Ari.instance)) return;
        this.isEnable = this.isEnable();
        this.commandCoolDown = this.getCommandCoolDown();
    }

    private boolean isEnable() {
        return Ari.instance.getConfig().getBoolean("server.command-cooldown.enable", false);
    }

    private int getCommandCoolDown() {
        return Ari.instance.getConfig().getInt("server.command-cooldown.value", 3);
    }

    private String getMainSubCommand(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.isEmpty()) {
            return "";
        }

        for (String name : PLUGIN_NAMES) {
            String prefixSpace = name + " ";
            String prefixColon = name + ":";

            if (command.regionMatches(true, 0, prefixSpace, 0, prefixSpace.length())) {
                String rest = command.substring(prefixSpace.length()).trim();
                if (rest.isEmpty()) return "";
                int spaceIdx = rest.indexOf(' ');
                return spaceIdx == -1 ? rest : rest.substring(0, spaceIdx);
            }

            if (command.regionMatches(true, 0, prefixColon, 0, prefixColon.length())) {
                String rest = command.substring(prefixColon.length()).trim();
                if (rest.isEmpty()) return "";
                int spaceIdx = rest.indexOf(' ');
                return spaceIdx == -1 ? rest : rest.substring(0, spaceIdx);
            }
        }

        for (String name : PLUGIN_NAMES) {
            if (command.equalsIgnoreCase(name)) {
                return "";
            }
        }

        int spaceIdx = command.indexOf(' ');
        return spaceIdx == -1 ? command : command.substring(0, spaceIdx);
    }

}
