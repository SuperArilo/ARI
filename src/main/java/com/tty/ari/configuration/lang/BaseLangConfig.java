package com.tty.ari.configuration.lang;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.enumType.FilePathEnum;
import com.tty.ari.configuration.BaseDownloadUrlConfig;

public class BaseLangConfig extends BaseDownloadUrlConfig {

    public BaseLangConfig(AbstractJavaPlugin plugin, FilePathEnum pathEnum) {
        super(plugin, pathEnum.getFullPathInJar().replace("[lang]", plugin.getConfig().getString("lang", "cn")));
    }

    public BaseLangConfig(AbstractJavaPlugin plugin) {
        super(plugin);
    }

}
