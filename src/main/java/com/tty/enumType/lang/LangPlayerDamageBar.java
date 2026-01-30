package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangPlayerDamageBar implements LangTypeEnum {

    MOB_UNRESOLVED("mob"),
    MOB_CURRENT_HEALTH_UNRESOLVED("mob_current_health"),
    MOB_MAX_HEALTH_UNRESOLVED("mob_max_health");

    private final String type;

    LangPlayerDamageBar(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
