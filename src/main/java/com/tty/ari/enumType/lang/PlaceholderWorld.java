package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderWorld implements PlaceholderTypeEnum {

    WORLD_WEATHER("world_weather");

    private final String type;

    PlaceholderWorld(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
