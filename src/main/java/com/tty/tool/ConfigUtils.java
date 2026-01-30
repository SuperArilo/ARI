package com.tty.tool;

import com.tty.Ari;
import com.tty.enumType.FilePath;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConfigUtils {

    public static CompletableFuture<Component> t(String key, Player player) {
        return Ari.PLACEHOLDER.render(key, player);
    }

    public static CompletableFuture<Component> t(String key, OfflinePlayer offlinePlayer) {
        return Ari.PLACEHOLDER.render(key, offlinePlayer);
    }

    public static CompletableFuture<Component> tList(String key, OfflinePlayer offlinePlayer) {
        return Ari.PLACEHOLDER.renderList(key, offlinePlayer);
    }

    public static CompletableFuture<Component> t(String key) {
        return Ari.PLACEHOLDER.render(key, null);
    }

    public static Component tAfter(String key, Map<String, Component> map) {
        return Ari.COMPONENT_SERVICE.text(Ari.C_INSTANCE.getValue(key, FilePath.LANG), map);
    }

    public static Component tAfter(String key) {
        return Ari.COMPONENT_SERVICE.text(Ari.C_INSTANCE.getValue(key, FilePath.LANG));
    }

}
