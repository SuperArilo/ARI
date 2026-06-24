package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderPlayerDeath implements PlaceholderTypeEnum {

    KILLER_UNRESOLVED("killer"),
    VICTIM_UNRESOLVED("victim"),
    KILLER_ITEM_UNRESOLVED("killer_item");

    private final String type;

    PlaceholderPlayerDeath(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
