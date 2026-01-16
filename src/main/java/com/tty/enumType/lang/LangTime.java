package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangTime implements LangTypeEnum {

    EXECUTE_TARGET_TIME("execute_target_time"),
    TIME_PERIOD("time_period"),
    SLEEP_PLAYERS("sleep_players"),
    SKIP_NIGHT_TICK_INCREMENT("skip_night_tick_increment");

    private final String type;

    LangTime(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
