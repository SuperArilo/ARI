package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerPlayer;
import com.tty.api.dto.PageResult;
import com.tty.api.BaseDataManager;
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
    public CompletableFuture<ServerPlayer> getInstance(LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                PlayersMapper mapper = session.getMapper(PlayersMapper.class);
                return mapper.selectOne(key);
            }
        });
    }

    @Override
    public CompletableFuture<ServerPlayer> createInstance(ServerPlayer instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                PlayersMapper mapper = session.getMapper(PlayersMapper.class);
                return mapper.insert(instance) == 1 ? instance:null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteInstance(ServerPlayer instance) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> modify(ServerPlayer instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                PlayersMapper mapper = session.getMapper(PlayersMapper.class);
                int update = mapper.update(instance, new LambdaQueryWrapper<ServerPlayer>().eq(ServerPlayer::getPlayerUUID, instance.getPlayerUUID()));
                return update == 1;
            }
        });
    }

}
