package com.tty.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SpawnLocation {

    @SerializedName("world")
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", this.worldName);
        map.put("x", this.x);
        map.put("y", this.y);
        map.put("z", this.z);
        map.put("pitch", this.pitch);
        map.put("yaw", this.yaw);
        return map;
    }

}
