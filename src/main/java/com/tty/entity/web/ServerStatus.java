package com.tty.entity.web;

import lombok.Data;

@Data
public class ServerStatus {

    private double tps;
    private double mspt;
    private double usedMemory;
    private long timestamp;
    private int players;

}
