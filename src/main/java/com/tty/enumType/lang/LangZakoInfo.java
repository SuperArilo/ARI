package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangZakoInfo implements LangTypeEnum {

    FIRST_LOGIN_SERVER_TIME("first_login_server_time"),
    LAST_LOGIN_SERVER_TIME("last_login_server_time"),
    TOTAL_TIME_ON_SERVER("total_online_time_on_server"),
    ZAKO_WHITELIST_OPERATOR("zako_whitelist_operator"),
    ZAKO_WHITELIST_ADD_TIME("zako_whitelist_add_time"),
    BAN_T0TAL_TIME("ban_total_time"),
    BAN_REASON("ban_reason"),
    BAN_END_TIME("ban_end_time");

    private final String type;

    LangZakoInfo(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
