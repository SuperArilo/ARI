package com.tty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tty.api.annotations.cache.CacheKey;
import lombok.Data;

@Data
@TableName("warps")
public class ServerWarp {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @CacheKey
    private String warpId;
    private String warpName;
    @CacheKey
    private String createBy;
    private String location;
    private String showMaterial;
    private String permission = "";
    private Double cost = 0.0;
    private Integer preSlot = Integer.MAX_VALUE;
    private boolean topSlot = false;
}
