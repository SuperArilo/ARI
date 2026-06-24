package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderShowItem implements PlaceholderTypeEnum {
    SHOW_ITEM("show_item");

    private final String type;

    PlaceholderShowItem(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
