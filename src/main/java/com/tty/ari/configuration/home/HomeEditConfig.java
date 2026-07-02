package com.tty.ari.configuration.home;

import com.tty.api.dto.gui.BaseMenu;
import com.tty.ari.configuration.common.GuiConfiguration;
import com.tty.ari.enumType.FilePath;

public class HomeEditConfig extends GuiConfiguration<BaseMenu> {

    public HomeEditConfig() {
        super(FilePath.HOME_EDIT_GUI.getPath());
    }

}
