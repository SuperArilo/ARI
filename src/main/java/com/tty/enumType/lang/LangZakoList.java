package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangZakoList implements LangTypeEnum {

    ZAKO_LIST_ITEM_NAME("zako_list_item_name"),
    ZAKO_LIST_ITEM_REMARK("zako_list_item_remark");

    private final String type;

    LangZakoList(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
