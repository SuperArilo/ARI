package com.tty.enumType.lang;

import com.tty.lib.enum_type.LangTypeEnum;

public enum LangPlayerChat  implements LangTypeEnum {

    SOURCE_DISPLAY_NAME("source_display_name"),
    CHAT_MESSAGE("message");

    private final String type;

    LangPlayerChat(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
