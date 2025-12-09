package com.tty.entity.sql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("whitelist")
public class WhitelistInstance {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("player_uuid")
    private String playerUUID;
    private double addTime;

}
