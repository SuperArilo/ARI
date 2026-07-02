package com.tty.ari.configuration;

import com.tty.api.configuration.AllowEnableConfiguration;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.enumType.FilePath;

import java.util.List;

public class AttackBarConfig extends AllowDownloadConfiguration implements AllowEnableConfiguration {

    public AttackBarConfig() {
        super(Ari.instance, FilePath.ATTACK_BAR_CONFIG.getPath());
    }

    @Override
    public boolean isEnable() {
        return this.getBool("attack-bar.enable", false);
    }

    public int getMaxBar() {
        return this.getInt("attack-bar.max-bar", 3);
    }

    public int getTickClearDealy() {
        return this.getInt("attack-bar.damage-tracker.tick_clear_dealy", 30);
    }

    public int getClearLastAttackRecord() {
        return this.getInt("attack-bar.damage-tracker.clear_last_attack_record", 20);
    }

    public List<String> getExcludedEntities() {
        return this.getStringList("attack-bar.damage-tracker.entities");
    }

}
