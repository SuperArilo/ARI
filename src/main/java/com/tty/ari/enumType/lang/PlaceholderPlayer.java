package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderPlayer implements PlaceholderTypeEnum {

    PLAYER_NAME("player_name"),
    PLAYER_WORLD("player_world"),
    PLAYER_LOCATION("player_location"),
    ENTITY_KILL_BY_PLAYER("entity_kill_by_player"),
    DEATH_LOCATION("death_location"),
    SPAWN_LOCATION("spawn_location");

    private final String type;

    PlaceholderPlayer(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
