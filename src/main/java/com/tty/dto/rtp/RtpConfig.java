package com.tty.dto.rtp;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class RtpConfig {
    @Expose
    private boolean enable = true;
    @Expose
    private Integer min = 300;
    @Expose
    private Integer max = 1500;
}
