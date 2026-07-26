package com.tty.ari.configuration;

import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.Ari;
import com.tty.ari.enumType.FilePath;

public class CommandAliasConfig extends BaseDownloadUrlConfig {

    public CommandAliasConfig() {
        super(Ari.instance, FilePath.COMMAND_ALIAS.getFullPathInJar());
    }

    public CommandAliasConfig(AbstractJavaPlugin plugin) {
        super(plugin);
    }

}
