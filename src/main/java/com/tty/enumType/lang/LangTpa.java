package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangTpa implements LangTypeEnum {

    TPA_SENDER("tpa_sender"),
    TPA_BE_SENDER("tpa_be_sender");

    private final String type;

    LangTpa(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
