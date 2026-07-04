package com.tty.ari.configuration;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.configuration.AllowDownloadConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.enumType.FilePath;

public class CommandAliasConfig extends AllowDownloadConfiguration {

    public CommandAliasConfig() {
        super(Ari.instance, FilePath.COMMAND_ALIAS.getPath());
    }

    public CommandAliasConfig(AbstractJavaPlugin plugin) {
        super(plugin);
    }
}
