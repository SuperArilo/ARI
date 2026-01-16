package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangPlayerDeath implements LangTypeEnum {

    KILLER("killer"),
    VICTIM("victim"),
    KILLER_ITEM("killer_item");

    private final String type;

    LangPlayerDeath(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
