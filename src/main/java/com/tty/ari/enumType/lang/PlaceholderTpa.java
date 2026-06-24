package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderTpa implements PlaceholderTypeEnum {

    TPA_SENDER("tpa_sender"),
    TPA_BE_SENDER("tpa_be_sender");

    private final String type;

    PlaceholderTpa(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
