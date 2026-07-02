package com.tty.ari.configuration;

import com.google.common.reflect.TypeToken;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.dto.SpawnLocation;
import com.tty.ari.dto.rtp.RtpConfig;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.TeleportType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FunctionConfig extends AllowDownloadConfiguration {

    public FunctionConfig() {
        super(Ari.instance, FilePath.FUNCTION_CONFIG.getPath());
    }

    public boolean isEnable(@NotNull TeleportType type) {
        return this.getBool(type.getKey() + ".enable", true);
    }

    public int getRtpSearchCount() {
        return this.getInt("rtp.search-count", 10);
    }

    public Map<String, RtpConfig> getRtpWorlds() {
        return this.getValue("rtp.worlds", new TypeToken<Map<String, RtpConfig>>(){}.getType(), null);
    }

    public int getTeleportDelay(@NotNull TeleportType type) {
        return this.getInt(type.getKey() + ".teleport.delay",  3);
    }

    public int getTeleportCooldown(@NotNull TeleportType type) {
        return this.getInt(type.getKey() + ".teleport.cooldown", 10);
    }

    public int getTpaRequestExpiredTime() {
        return this.getInt("tpa.teleport.request-expired-time", 10);
    }

    public boolean getSpawnFirstJoin() {
        return this.getValue("spawn.first-join", Boolean.class, false);
    }

    public SpawnLocation getSpawnLocation() {
        return this.getValue("spawn.location", new TypeToken<SpawnLocation>(){}.getType(), null);
    }

}
