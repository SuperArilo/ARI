package com.tty.enumType.lang;

import com.tty.api.enumType.LangTypeEnum;

public enum LangRTP  implements LangTypeEnum {

    RTP_SEARCH_COUNT("rtp_search_count");

    private final String type;

    LangRTP(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
