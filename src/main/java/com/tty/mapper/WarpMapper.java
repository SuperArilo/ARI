package com.tty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.entity.sql.ServerWarp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarpMapper extends BaseMapper<ServerWarp> {
}
