package com.tty.ari.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.ari.entity.ServerPlayer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlayersMapper extends BaseMapper<ServerPlayer> {
}
