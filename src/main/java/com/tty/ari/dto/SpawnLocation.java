package com.tty.ari.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SpawnLocation {

    @Expose
    @SerializedName("world")
    private String worldName;
    @Expose
    private double x;
    @Expose
    private double y;
    @Expose
    private double z;
    @Expose
    private float pitch;
    @Expose
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
