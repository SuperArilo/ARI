package com.tty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.entity.sql.ServerHome;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeMapper extends BaseMapper<ServerHome> {
}
