package com.tty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.entity.WhitelistInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WhitelistMapper extends BaseMapper<WhitelistInstance> {
}
