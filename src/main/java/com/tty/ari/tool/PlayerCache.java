package com.tty.ari.tool;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class PlayerCache {

    private static final Pattern PLAYER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    private static final Cache<@NotNull UUID, OfflinePlayer> CACHE =
            Caffeine.newBuilder()
                    .maximumSize(2000)
                    .expireAfterWrite(300, TimeUnit.MINUTES)
                    .build(Bukkit::getOfflinePlayer);


    public static @NotNull OfflinePlayer getPlayer(UUID uuid) {
        OfflinePlayer present = CACHE.getIfPresent(uuid);
        if (present != null) {
            return present;
        }
        OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
        CACHE.put(uuid, offlinePlayer);
        return offlinePlayer;
    }

    public static @Nullable OfflinePlayer getPlayer(String value) {
        UUID uuid;
        try {
            uuid = UUID.fromString(value);
            return getPlayer(uuid);
        } catch (Exception e) {
            if (!PLAYER_ID_PATTERN.matcher(value).matches()) return null;
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(value);
            CACHE.put(offlinePlayer.getUniqueId(), offlinePlayer);
            return offlinePlayer;
        }
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
