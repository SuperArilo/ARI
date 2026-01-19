package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangPlayerDeath implements LangTypeEnum {

    KILLER_UNRESOLVED("killer"),
    VICTIM_UNRESOLVED("victim"),
    KILLER_ITEM_UNRESOLVED("killer_item");

    private final String type;

    LangPlayerDeath(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
