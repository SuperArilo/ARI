package com.tty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tty.api.annotations.entity.CacheKey;
import lombok.Data;

@Data
@TableName("player_home")
public class ServerHome {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @CacheKey
    private String homeId;
    private String homeName;
    @CacheKey
    @TableField("player_uuid")
    private String playerUUID;
    private String location;
    private String showMaterial;
    @CacheKey
    private Integer preSlot = Integer.MAX_VALUE;
    private boolean topSlot = false;
}
