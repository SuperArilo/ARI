package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderPlayerChat implements PlaceholderTypeEnum {

    SOURCE_DISPLAY_NAME_UNRESOLVED("source_display_name"),
    CHAT_MESSAGE_UNRESOLVED("message");

    private final String type;

    PlaceholderPlayerChat(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
