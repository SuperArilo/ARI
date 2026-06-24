package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderPlayerDamageBar implements PlaceholderTypeEnum {

    MOB_UNRESOLVED("mob"),
    MOB_CURRENT_HEALTH_UNRESOLVED("mob_current_health"),
    MOB_MAX_HEALTH_UNRESOLVED("mob_max_health");

    private final String type;

    PlaceholderPlayerDamageBar(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
