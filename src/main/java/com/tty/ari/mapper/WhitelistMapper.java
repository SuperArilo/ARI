package com.tty.ari.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.ari.entity.WhitelistInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WhitelistMapper extends BaseMapper<WhitelistInstance> {
}
