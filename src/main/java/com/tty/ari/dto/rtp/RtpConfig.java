package com.tty.ari.dto.rtp;

import com.google.gson.annotations.Expose;
import lombok.Getter;

public class RtpConfig {

    @Expose
    @Getter
    private boolean enable = true;
    @Expose
    @Getter
    private Integer min = 300;
    @Expose
    @Getter
    private Integer max = 1500;

    public RtpConfig() {}

    public RtpConfig(boolean enable, int min, int max) {
        this.enable = enable;
        this.min = min;
        this.max = max;
    }

}
