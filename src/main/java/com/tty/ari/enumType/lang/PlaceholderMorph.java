package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderMorph implements PlaceholderTypeEnum {

    MORPH_ENTITY("morph_entity");

    private final String type;

    PlaceholderMorph(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
