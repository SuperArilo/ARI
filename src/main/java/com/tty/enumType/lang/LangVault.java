package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangVault implements LangTypeEnum {

    COSTED_UNRESOLVED("costed");

    private final String type;

    LangVault(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
