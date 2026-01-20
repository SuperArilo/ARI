package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangZakoList implements LangTypeEnum {

    ZAKO_LIST_ITEM_NAME("zako_list_item_name");

    private final String type;

    LangZakoList(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
