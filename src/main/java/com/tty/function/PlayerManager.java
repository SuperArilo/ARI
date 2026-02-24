package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerPlayer;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.mapper.PlayersMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.CompletableFuture;

public class PlayerManager extends BaseDataManager<ServerPlayer> {

    public PlayerManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerPlayer>> getList(int pageNum, int pageSize, LambdaQueryWrapper<ServerPlayer> key) {
        return null;
    }

    @Override
    public CompletableFuture<ServerPlayer> get(LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                return session.getMapper(PlayersMapper.class).selectOne(key);
            }
        });
    }

    @Override
    public CompletableFuture<ServerPlayer> create(ServerPlayer instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(PlayersMapper.class).insert(instance) == 1 ? instance:null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(ServerPlayer entity) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> delete(LambdaQueryWrapper<ServerPlayer> key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> update(ServerPlayer instance, LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(PlayersMapper.class).update(instance, key) == 1;
            }
        });
    }

}
