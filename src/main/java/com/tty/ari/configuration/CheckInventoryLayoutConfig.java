package com.tty.ari.configuration;

import com.tty.ari.configuration.common.GuiConfiguration;
import com.tty.ari.dto.gui.PlayerInventoryCheckMenu;
import com.tty.ari.enumType.FilePath;

public class CheckInventoryLayoutConfig extends GuiConfiguration<PlayerInventoryCheckMenu> {

    public CheckInventoryLayoutConfig() {
        super(FilePath.INV_GUI_CONFIG.getPath());
    }

}
