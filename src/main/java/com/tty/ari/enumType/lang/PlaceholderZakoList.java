package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderZakoList implements PlaceholderTypeEnum {

    ZAKO_LIST_ITEM_NAME("zako_list_item_name"),
    ZAKO_LIST_ITEM_REMARK("zako_list_item_remark");

    private final String type;

    PlaceholderZakoList(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
