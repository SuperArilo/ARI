package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangPlayerDamageBar implements LangTypeEnum {

    MOB("mob"),
    MOB_CURRENT_HEALTH("mob_current_health"),
    MOB_MAX_HEALTH("mob_max_health");

    private final String type;

    LangPlayerDamageBar(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
