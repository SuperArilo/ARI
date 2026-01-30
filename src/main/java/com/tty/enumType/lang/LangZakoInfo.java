package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangZakoInfo implements LangTypeEnum {

    FIRST_LOGIN_SERVER_TIME("first_login_server_time"),
    LAST_LOGIN_SERVER_TIME("last_login_server_time"),
    TOTAL_TIME_ON_SERVER("total_online_time_on_server"),
    ZAKO_WHITELIST_OPERATOR("zako_whitelist_operator"),
    ZAKO_WHITELIST_ADD_TIME("zako_whitelist_add_time");

    private final String type;

    LangZakoInfo(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
