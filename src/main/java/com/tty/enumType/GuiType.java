package com.tty.enumType;

import com.tty.api.enumType.GuiKeyEnum;

public enum GuiType implements GuiKeyEnum {

    HOME_LIST("home_list"),
    HOME_EDIT("home_edit"),
    WARP_LIST("warp_list"),
    WARP_EDIT("warp_edit"),
    OFFLINE_ENDERCHEST("offline_enderchest");

    private final String type;

    GuiType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
