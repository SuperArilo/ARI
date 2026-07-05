package com.tty.ari.tool;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerCache {

    private static final Cache<@NotNull UUID, OfflinePlayer> CACHE =
            Caffeine.newBuilder()
                    .maximumSize(2000)
                    .expireAfterWrite(300, TimeUnit.MINUTES)
                    .build();


    public static @NotNull OfflinePlayer getPlayer(UUID uuid) {
        OfflinePlayer present = CACHE.getIfPresent(uuid);
        if (present != null) {
            return present;
        }
        OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
        CACHE.put(uuid, offlinePlayer);
        return offlinePlayer;
    }

    public static void removePlayer(UUID uuid) {
        CACHE.invalidate(uuid);
    }

    public static void addPlayer(OfflinePlayer offlinePlayer) {
        CACHE.put(offlinePlayer.getUniqueId(), offlinePlayer);
    }

    public static void clean() {
        CACHE.invalidateAll();
    }

}
