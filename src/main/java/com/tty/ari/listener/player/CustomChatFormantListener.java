package com.tty.ari.listener.player;

import com.google.gson.reflect.TypeToken;
import com.tty.api.event.CustomPluginReloadEvent;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerChatState;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.lang.PlaceholderPlayerChat;
import com.tty.ari.states.PlayerChatService;
import com.tty.ari.tool.ConfigUtils;
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
    private boolean chatIsEnable;
    private boolean cooldownIsEnable;
    private int cooldownTime;

    public CustomChatFormantListener() {
        this.groupsPattern = this.getGroupsPattern();
        this.chatIsEnable = this.isChatIsEnable();
        this.cooldownIsEnable = this.isCooldownIsEnable();
        this.cooldownTime = this.getCooldownTime();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerSendMessage(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (this.chatIsEnable) {
            if (this.cooldownIsEnable) {
                if(!Ari.STATE_MACHINE_MANAGER.get(PlayerChatService.class).addState(new PlayerChatState(player, event.message(), this.cooldownTime))) {
                    ConfigUtils.t("server.message.chat-cooldown", player).thenAccept(player::sendMessage);
                    event.setCancelled(true);
                    return;
                }
            }
           event.renderer((source, sourceDisplayName, msg, viewer) ->
                   Ari.instance.getComponentTool().text(this.getPattern(source), player, Map.of(PlaceholderPlayerChat.SOURCE_DISPLAY_NAME_UNRESOLVED.getType(), Component.text(source.getName()), PlaceholderPlayerChat.CHAT_MESSAGE_UNRESOLVED.getType(), msg)));
       }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void whenPluginReload(CustomPluginReloadEvent event) {
        this.chatIsEnable = this.isChatIsEnable();
        this.cooldownTime = this.getCooldownTime();
        this.cooldownIsEnable = this.isCooldownIsEnable();
        this.groupsPattern = this.getGroupsPattern();
    }

    private Map<String, String> getGroupsPattern() {
        return Ari.instance.getConfigInstance().getValue("chat.groups-pattern", FilePath.CHAT_CONFIG, new TypeToken<Map<String, String>>(){}.getType(), Map.of());
    }

    private String getPattern(Player player) {
        AtomicReference<String> s = new AtomicReference<>("");
        this.groupsPattern.forEach((k, v) -> {
            if (!s.get().isEmpty()) return;
            if (Ari.PERMISSION_SERVICE.getPlayerIsInGroup(player, k)) {
                s.set(v);
            }
        });
        if (s.get().isEmpty()) {
            s.set(this.groupsPattern.get("_default_"));
        }
        return s.get();
    }

    private boolean isChatIsEnable() {
        return Ari.instance.getConfigInstance().getValue("chat.enable", FilePath.CHAT_CONFIG, Boolean.class, false);
    }

    private boolean isCooldownIsEnable() {
        return Ari.instance.getConfigInstance().getValue("chat.cooldown.enable", FilePath.CHAT_CONFIG, Boolean.class, false);
    }

    private int getCooldownTime() {
        return Ari.instance.getConfigInstance().getValue("chat.cooldown.value", FilePath.CHAT_CONFIG, Integer.class, 2);
    }

}
