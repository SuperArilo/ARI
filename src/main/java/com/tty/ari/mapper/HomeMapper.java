package com.tty.ari.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.ari.entity.ServerHome;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeMapper extends BaseMapper<ServerHome> {
}
