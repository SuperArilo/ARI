package com.tty.enumType;

import com.tty.mapper.HomeMapper;
import com.tty.mapper.PlayersMapper;
import com.tty.mapper.WarpMapper;
import com.tty.mapper.WhitelistMapper;
import lombok.Getter;

@Getter
public enum Mapper {
    PLAYERS(PlayersMapper.class),
    WARP(WarpMapper.class),
    HOME(HomeMapper.class),
    WHITELIST(WhitelistMapper.class);

    private final Class<?> mapperClass;

    Mapper(Class<?> mapperClass) {
        this.mapperClass = mapperClass;
    }
}
