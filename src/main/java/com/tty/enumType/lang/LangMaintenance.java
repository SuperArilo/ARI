package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangMaintenance implements LangTypeEnum {

    MAINTENANCE_KICK_DEALY("maintenance_kick_delay");

    private final String type;

    LangMaintenance(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
