package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangPlayerChat implements LangTypeEnum {

    SOURCE_DISPLAY_NAME_UNRESOLVED("source_display_name"),
    CHAT_MESSAGE_UNRESOLVED("message");

    private final String type;

    LangPlayerChat(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
