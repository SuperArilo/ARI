package com.tty.ari.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.tty.api.annotations.cache.CacheKey;
import lombok.Data;

@Data
@TableName("players")
public class ServerPlayer {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String playerName;
    @TableField("player_uuid")
    @CacheKey
    private String playerUUID;
    private Long firstLoginTime = 0L;
    private Long lastLoginOffTime = 0L;
    private Long totalOnlineTime = 0L;
    @TableField(value = "name_prefix", updateStrategy = FieldStrategy.ALWAYS)
    private String namePrefix;
    @TableField(value = "name_suffix", updateStrategy = FieldStrategy.ALWAYS)
    private String nameSuffix;
}
