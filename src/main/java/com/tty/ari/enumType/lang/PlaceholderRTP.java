package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderRTP implements PlaceholderTypeEnum {

    RTP_SEARCH_COUNT("rtp_search_count");

    private final String type;

    PlaceholderRTP(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
