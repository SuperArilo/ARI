package com.tty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tty.api.annotations.cache.CacheKey;
import lombok.Data;

@Data
@TableName("player_home")
public class ServerHome {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @CacheKey
    private String homeId;
    private String homeName;
    @TableField("player_uuid")
    @CacheKey
    private String playerUUID;
    private String location;
    private String showMaterial;
    private Integer preSlot = Integer.MAX_VALUE;
    private boolean topSlot = false;
}
