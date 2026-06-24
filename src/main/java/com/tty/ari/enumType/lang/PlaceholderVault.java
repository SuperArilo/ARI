package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderVault implements PlaceholderTypeEnum {

    COSTED_UNRESOLVED("costed");

    private final String type;

    PlaceholderVault(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
