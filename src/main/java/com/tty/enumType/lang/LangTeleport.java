package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangTeleport implements LangTypeEnum {

    TELEPORT_DELAY("teleport_delay");

    private final String type;

    LangTeleport(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
