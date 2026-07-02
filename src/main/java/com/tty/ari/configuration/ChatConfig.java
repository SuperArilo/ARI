package com.tty.ari.configuration;

import com.google.common.reflect.TypeToken;
import com.tty.api.configuration.AllowEnableConfiguration;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.enumType.FilePath;

import java.util.Map;

public class ChatConfig extends AllowDownloadConfiguration implements AllowEnableConfiguration {

    public ChatConfig() {
        super(Ari.instance, FilePath.CHAT_CONFIG.getPath());
    }

    @Override
    public boolean isEnable() {
        return this.getBool("chat.enable", false);
    }

    public boolean isCooldownEnable() {
        return this.getBool("chat.cooldown.enable", true);
    }

    public int cooldownValue() {
        return this.getInt("chat.cooldown.value", 2);
    }

    public Map<String, String> getGroupsPattern() {
        return this.getValue("chat.groups-pattern", new TypeToken<Map<String, String>>(){}.getType(), Map.of());
    }

}
