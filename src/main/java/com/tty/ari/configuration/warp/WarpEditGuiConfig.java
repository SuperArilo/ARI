package com.tty.ari.configuration.warp;

import com.tty.api.dto.gui.BaseMenu;
import com.tty.ari.configuration.common.GuiConfiguration;
import com.tty.ari.enumType.FilePath;

public class WarpEditGuiConfig extends GuiConfiguration<BaseMenu> {
    public WarpEditGuiConfig() {
        super(FilePath.WARP_EDIT_GUI.getPath());
    }
}
