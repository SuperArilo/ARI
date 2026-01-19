package com.tty.listener.player;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.enumType.lang.LangPlayerChat;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.PermissionUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CustomChatFormantListener implements Listener {

    private Map<String, String> groupsPattern = new HashMap<>();

    public CustomChatFormantListener() {
        this.groupsPattern = this.set();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerSendMessage(AsyncChatEvent event) {
        if (this.isNotEnable()) return;
        event.renderer((source, sourceDisplayName, msg, viewer) ->
                ComponentUtils.text(
                        this.getPattern(source),
                        Map.of(LangPlayerChat.SOURCE_DISPLAY_NAME_UNRESOLVED.getType(), Component.text(source.getName()), LangPlayerChat.CHAT_MESSAGE_UNRESOLVED.getType(), msg)));
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void whenPluginReload(CustomPluginReloadEvent event) {
        if (this.isNotEnable()) return;
        this.groupsPattern = this.set();
    }

    private Map<String, String> set() {
        return Ari.C_INSTANCE.getValue("chat.groups-pattern", FilePath.FUNCTION_CONFIG, new TypeToken<Map<String, String>>(){}.getType(), Map.of());
    }

    private String getPattern(Player player)                      {
        AtomicReference<String> s = new AtomicReference<>("");
        this.groupsPattern.forEach((k, v) -> {
            if (!s.get().isEmpty()) return;
            if (PermissionUtils.getPlayerIsInGroup(player, k)) {
                s.set(v);
            }
        });
        if (s.get().isEmpty()) {
            s.set(this.groupsPattern.get("_default_"));
        }
        return s.get();
    }

    private boolean isNotEnable() {
        return !Ari.C_INSTANCE.getValue("chat.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }
}
