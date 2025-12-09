package com.tty.entity.sql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("player_home")
public class ServerHome {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String homeId;
    private String homeName;
    @TableField("player_uuid")
    private String playerUUID;
    private String location;
    private String showMaterial;
    private boolean topSlot = false;
}
