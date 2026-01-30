package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangShowItem implements LangTypeEnum {
    SHOW_ITEM("show_item");

    private final String type;

    LangShowItem(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
