package com.tty.entity.sql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("warps")
public class ServerWarp extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String warpId;
    private String warpName;
    private String createBy;
    private String location;
    private String showMaterial;
    private String permission = "";
    private Double cost = 0.0;
    private boolean topSlot = false;

}
