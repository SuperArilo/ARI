package com.tty.ari.tool;

import com.tty.ari.Ari;
import com.tty.ari.configuration.lang.LangConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConfigUtils {

    public static CompletableFuture<Component> t(String key, Player player) {
        return Ari.instance.getEngine().render(Ari.instance.getConfigurationManager().get(LangConfig.class).getString(key), player);
    }

    public static CompletableFuture<Component> t(String key, OfflinePlayer offlinePlayer) {
        return Ari.instance.getEngine().render(Ari.instance.getConfigurationManager().get(LangConfig.class).getString(key), offlinePlayer);
    }

    public static CompletableFuture<Component> tList(String key, OfflinePlayer offlinePlayer) {
        return Ari.instance.getEngine().renderList(Ari.instance.getConfigurationManager().get(LangConfig.class).getStringList(key), offlinePlayer);
    }

    public static CompletableFuture<Component> tList(String key) {
        return Ari.instance.getEngine().renderList(Ari.instance.getConfigurationManager().get(LangConfig.class).getStringList(key), null);
    }

    public static CompletableFuture<List<Component>> tAsList(String key) {
        return Ari.instance.getEngine().renderAsComponentList(Ari.instance.getConfigurationManager().get(LangConfig.class).getStringList(key), null);
    }

    public static CompletableFuture<List<Component>> tAsList(String key, Player player) {
        return Ari.instance.getEngine().renderAsComponentList(Ari.instance.getConfigurationManager().get(LangConfig.class).getStringList(key), player);
    }

    public static CompletableFuture<List<Component>> tAsList(String key, OfflinePlayer offlinePlayer) {
        return Ari.instance.getEngine().renderAsComponentList(Ari.instance.getConfigurationManager().get(LangConfig.class).getStringList(key), offlinePlayer);
    }

    public static CompletableFuture<Component> t(String key) {
        return Ari.instance.getEngine().render(Ari.instance.getConfigurationManager().get(LangConfig.class).getString(key), null);
    }

    public static Component tAfter(String key, Map<String, Component> map) {
        return Ari.instance.getEngine().directRender(Ari.instance.getConfigurationManager().get(LangConfig.class).getString(key), map);
    }

    public static Component tAfter(String key) {
        return Ari.instance.getEngine().directRender(Ari.instance.getConfigurationManager().get(LangConfig.class).getString(key));
    }

}
