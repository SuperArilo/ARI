package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangPlayer implements LangTypeEnum {

    PLAYER_NAME("player_name"),
    PLAYER_WORLD("player_world"),
    PLAYER_LOCATION("player_location"),
    DEATH_LOCATION("death_location"),
    SPAWN_LOCATION("spawn_location");

    private final String type;

    LangPlayer(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
