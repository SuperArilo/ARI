package com.tty.ari.configuration.warp;

import com.tty.api.configuration.AllowEnableConfiguration;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.configuration.common.TeleportConfiguration;
import com.tty.ari.enumType.FilePath;

import java.util.List;

public class WarpConfig extends AllowDownloadConfiguration implements TeleportConfiguration, AllowEnableConfiguration {

    public WarpConfig() {
        super(Ari.instance, FilePath.WARP_CONFIG.getPath());
    }

    @Override
    public boolean isEnable() {
        return this.getBool("main.enable", true);
    }

    @Override
    public int getDelay() {
        return this.getInt("main.teleport.delay", 3);
    }

    @Override
    public int getCooldown() {
        return this.getInt("main.teleport.cooldown", 10);
    }

    public boolean cost() {
        return this.getBool("main.cost", false);
    }

    public boolean permission() {
        return this.getBool("main.permission", false);
    }

    public int getWarpNameLength() {
        return this.getInt("main.name-length", 12);
    }

    public List<String> checkWarpNickName() {
        return this.getStringList("main.name-check");
    }

}
