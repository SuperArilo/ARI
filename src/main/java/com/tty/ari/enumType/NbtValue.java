package com.tty.ari.enumType;

import com.tty.api.enumType.NbtEnum;

public enum NbtValue implements NbtEnum {
    GUI_TYPE("gui_type");

    private final String key;

    NbtValue(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
