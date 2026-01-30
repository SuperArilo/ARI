package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangBanPlayerType implements LangTypeEnum {

    BAN_T0TAL_TIME("ban_total_time"),
    BAN_REASON("ban_reason"),
    BAN_END_TIME("ban_end_time");

    private final String type;

    LangBanPlayerType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
