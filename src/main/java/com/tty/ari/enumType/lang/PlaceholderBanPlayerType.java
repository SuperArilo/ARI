package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderBanPlayerType implements PlaceholderTypeEnum {

    BAN_PLAYER_NAME("ban_player_name"),
    BAN_T0TAL_TIME("ban_total_time"),
    BAN_REASON("ban_reason"),
    BAN_END_TIME("ban_end_time");

    private final String type;

    PlaceholderBanPlayerType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
