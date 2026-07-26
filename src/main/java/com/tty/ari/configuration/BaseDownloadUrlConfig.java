package com.tty.ari.configuration;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.configuration.AllowDownloadConfiguration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class BaseDownloadUrlConfig extends AllowDownloadConfiguration {

    protected static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/SuperArilo/Plugin-Configs/main/";

    private final AbstractJavaPlugin plugin;
    private String relativePath;

    public BaseDownloadUrlConfig(AbstractJavaPlugin plugin, String relativePath) {
        super(plugin, relativePath);
        this.plugin = plugin;
        this.relativePath = relativePath;
    }

    public BaseDownloadUrlConfig(AbstractJavaPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getDownloadUrl() {
        return DOWNLOAD_URL + this.plugin.getName() + "/" + Arrays.stream(this.relativePath.replace("[lang]", plugin.getConfig().getString("lang", "cn")).split("/")).map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8)).collect(Collectors.joining("/"));
    }

}
