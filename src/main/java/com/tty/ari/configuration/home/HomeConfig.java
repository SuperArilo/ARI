package com.tty.ari.configuration.home;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.configuration.AllowEnableConfiguration;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.configuration.common.TeleportConfiguration;
import com.tty.ari.enumType.FilePath;

import java.util.List;

public class HomeConfig extends AllowDownloadConfiguration implements TeleportConfiguration, AllowEnableConfiguration {

    public HomeConfig() {
        super(Ari.instance, FilePath.HOME_CONFIG.getPath());
    }

    public HomeConfig(AbstractJavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isEnable() {
        return this.getBool("main.enable", true);
    }

    public int getHomeNameLength() {
        return this.getInt("main.name-length", 12);
    }


    @Override
    public int getDelay() {
        return this.getInt("main.teleport.delay", 3);
    }

    @Override
    public int getCooldown() {
        return this.getInt("main.teleport.cooldown", 10);
    }

    public List<String> getCheckHomeName() {
        return this.getStringList("main.name-check");
    }

}
