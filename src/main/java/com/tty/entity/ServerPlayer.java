package com.tty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("players")
public class ServerPlayer {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String playerName;
    @TableField("player_uuid")
    private String playerUUID;
    private Long firstLoginTime = 0L;
    private Long lastLoginOffTime = 0L;
    private Long totalOnlineTime = 0L;
    private String namePrefix = "";
    private String nameSuffix = "";
}
