package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderTeleport implements PlaceholderTypeEnum {

    TELEPORT_DELAY("teleport_delay");

    private final String type;

    PlaceholderTeleport(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
