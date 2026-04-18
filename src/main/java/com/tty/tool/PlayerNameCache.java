package com.tty.tool;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlayerNameCache {

    private static final Cache<@NotNull UUID, String> CACHE =
            Caffeine.newBuilder()
                    .maximumSize(2000)
                    .expireAfterWrite(300, TimeUnit.MINUTES)
                    .build();

    public static @NotNull String getName(@NotNull UUID uuid) {
        String name = CACHE.getIfPresent(uuid);
        if (name != null) {
            return name;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        name = offlinePlayer.getName();
        if (name != null) {
            CACHE.put(uuid, name);
            return name;
        }
        return "";
    }

    public static void update(@NotNull UUID uuid, @NotNull String name) {
        CACHE.put(uuid, name);
    }

    /**
     * 清除指定 UUID 的缓存条目
     * @param uuid 玩家 UUID
     */
    public static void invalidate(@NotNull UUID uuid) {
        CACHE.invalidate(uuid);
    }

    public static void clear() {
        CACHE.invalidateAll();
    }
}