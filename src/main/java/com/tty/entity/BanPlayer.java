package com.tty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tty.api.annotations.cache.CacheKey;
import lombok.Data;

@TableName("bad_list")
@Data
public class BanPlayer {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @CacheKey
    @TableField(value = "player_uuid")
    private String playerUUID;
    @TableField(value = "operator")
    @CacheKey
    private String operator;
    @TableField(value = "start_time")
    private Long startTime;
    @TableField(value = "end_time")
    private Long endTime;
    @TableField(value = "reason")
    private String reason;
}
