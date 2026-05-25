package com.tty.ari.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangServer  implements LangTypeEnum {

    SERVER_VERSION("server_version"),
    ARI_VERSION("ari_version"),
    ARI_DEBUG_STATUS("ari_debug_status");

    private final String type;

    LangServer(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
