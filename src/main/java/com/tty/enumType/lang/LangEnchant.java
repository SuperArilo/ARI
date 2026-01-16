package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangEnchant implements LangTypeEnum {

    ENCHANT_NAME("enchant_name"),
    ENCHANT_LEVEL("enchant_level");

    private final String type;

    LangEnchant(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
