package com.tty.ari.configuration;

import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.enumType.FilePath;

import java.util.List;

public class GameActionConfig extends AllowDownloadConfiguration {

    public GameActionConfig() {
        super(Ari.instance, FilePath.GAME_ACTION_CONFIG.getPath());
    }

    public boolean isSitEnable() {
        return this.getBool("action.sit.enable", false);
    }

    public boolean isPlayerSitPlayerEnable() {
        return this.getBool("action.player-sit-player.enable", false);
    }

    public List<String> getSitDisableBlock() {
        return this.getStringList("action.sit.disable-block");
    }

}
