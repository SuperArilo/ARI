package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderTime implements PlaceholderTypeEnum {

    TIME_PERIOD_UNRESOLVED("time_period"),
    EXECUTE_TARGET_TIME("execute_target_time"),
    SLEEP_PLAYERS("sleep_players"),
    SKIP_NIGHT_TICK_INCREMENT("skip_night_tick_increment");

    private final String type;

    PlaceholderTime(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
