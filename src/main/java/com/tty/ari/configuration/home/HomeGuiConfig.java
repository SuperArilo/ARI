package com.tty.ari.configuration.home;

import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.ari.configuration.common.GuiConfiguration;
import com.tty.ari.enumType.FilePath;

public class HomeGuiConfig extends GuiConfiguration<BaseDataMenu> {

    public HomeGuiConfig() {
        super(FilePath.HOME_LIST_GUI.getPath());
    }

}
