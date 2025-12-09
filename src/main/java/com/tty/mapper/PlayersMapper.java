package com.tty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.entity.sql.ServerPlayer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlayersMapper extends BaseMapper<ServerPlayer> {
}
