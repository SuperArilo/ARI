package com.tty.ari.configuration;

import com.google.common.reflect.TypeToken;
import com.tty.api.AbstractJavaPlugin;
import com.tty.api.configuration.AllowEnableConfiguration;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.dto.tab.TabGroupLine;
import com.tty.ari.enumType.FilePath;

import java.util.List;
import java.util.Map;

public class TabListConfig extends AllowDownloadConfiguration implements AllowEnableConfiguration {

    public TabListConfig() {
        super(Ari.instance, FilePath.TAB_LIST_CONFIG.getPath());
    }

    public TabListConfig(AbstractJavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isEnable() {
        return this.getBool("tab.enable", false);
    }

    public int updateInterval() {
        return this.getInt("tab.update-interval", 20);
    }

    public List<String> getLayoutHeader() {
        return this.getStringList("tab.layout.header");
    }

    public List<String> getLayoutFooter() {
        return this.getStringList("tab.layout.footer");
    }

    public Map<String, TabGroupLine> getGroups() {
        return this.getValue("tab.groups", new TypeToken<Map<String, TabGroupLine>>() {}.getType(), Map.of());
    }

    public List<String> getSlot() {
        return this.getStringList("tab.slot");
    }

}
