package com.tty.ari.enumType;

import com.tty.api.enumType.NbtEnum;

public enum PlayerNbt implements NbtEnum {

    VANISH("player_vanish");

    private final String type;

    PlayerNbt(String string) {
        this.type = string;
    }

    @Override
    public String getKey() {
        return this.type;
    }

}
