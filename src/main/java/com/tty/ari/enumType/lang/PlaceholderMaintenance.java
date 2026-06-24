package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderMaintenance implements PlaceholderTypeEnum {

    MAINTENANCE_KICK_DEALY("maintenance_kick_delay");

    private final String type;

    PlaceholderMaintenance(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
