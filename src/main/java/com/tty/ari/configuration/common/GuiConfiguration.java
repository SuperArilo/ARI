package com.tty.ari.configuration.common;

import com.tty.api.dto.gui.BaseMenu;
import com.tty.ari.Ari;
import com.tty.ari.configuration.BaseDownloadUrlConfig;

public class GuiConfiguration<T extends BaseMenu> extends BaseDownloadUrlConfig {

    public GuiConfiguration(String path) {
        super(Ari.instance, path);
    }

    public T getMenuConfig(Class<T> type) {
        return Ari.instance.getConfigurationManager().yamlConvertToObj(this.getConfiguration().saveToString(), type);
    }

}
