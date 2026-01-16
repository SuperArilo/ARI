package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangVault implements LangTypeEnum {

    COSTED("costed");

    private final String type;

    LangVault(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
