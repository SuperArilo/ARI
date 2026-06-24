package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderEnchant implements PlaceholderTypeEnum {

    ENCHANT_NAME_UNRESOLVED("enchant_name"),
    ENCHANT_LEVEL_UNRESOLVED("enchant_level");

    private final String type;

    PlaceholderEnchant(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
