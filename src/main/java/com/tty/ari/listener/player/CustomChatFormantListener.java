package com.tty.ari.listener.player;

import com.tty.ari.Ari;
import com.tty.ari.configuration.ChatConfig;
import com.tty.ari.dto.state.player.PlayerChatState;
import com.tty.ari.enumType.lang.PlaceholderPlayerChat;
import com.tty.ari.states.PlayerChatService;
import com.tty.ari.tool.ConfigUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CustomChatFormantListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerSendMessage(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatConfig chatConfig = Ari.instance.getConfigurationManager().get(ChatConfig.class);
        if (!chatConfig.isEnable()) return;
        if (chatConfig.isCooldownEnable()) {
            if(!Ari.STATE_MACHINE_MANAGER.get(PlayerChatService.class).addState(new PlayerChatState(player, event.message(), chatConfig.cooldownValue()))) {
                ConfigUtils.t("server.message.chat-cooldown", player).thenAccept(player::sendMessage);
                event.setCancelled(true);
                return;
            }
        }
        event.renderer((source, sourceDisplayName, msg, viewer) ->
                Ari.instance.getComponentTool().text(this.getPattern(source), player, Map.of(PlaceholderPlayerChat.SOURCE_DISPLAY_NAME_UNRESOLVED.getType(), Component.text(source.getName()), PlaceholderPlayerChat.CHAT_MESSAGE_UNRESOLVED.getType(), msg)));
    }

    private String getPattern(Player player) {
        AtomicReference<String> s = new AtomicReference<>("");
        Map<String, String> groupsPattern = Ari.instance.getConfigurationManager().get(ChatConfig.class).getGroupsPattern();
        groupsPattern.forEach((k, v) -> {
            if (!s.get().isEmpty()) return;
            if (Ari.PERMISSION_SERVICE.getPlayerIsInGroup(player, k)) {
                s.set(v);
            }
        });
        if (s.get().isEmpty()) {
            s.set(groupsPattern.get("_default_"));
        }
        return s.get();
    }

}
