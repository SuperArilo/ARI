package com.tty.ari.configuration.warp;

import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.ari.configuration.common.GuiConfiguration;
import com.tty.ari.enumType.FilePath;

public class WarpGuiConfig extends GuiConfiguration<BaseDataMenu> {

    public WarpGuiConfig() {
        super(FilePath.WARP_LIST_GUI.getPath());
    }

}
