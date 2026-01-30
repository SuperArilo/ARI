package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangEnchant implements LangTypeEnum {

    ENCHANT_NAME_UNRESOLVED("enchant_name"),
    ENCHANT_LEVEL_UNRESOLVED("enchant_level");

    private final String type;

    LangEnchant(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
