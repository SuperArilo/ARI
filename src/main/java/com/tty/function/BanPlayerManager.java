package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.BanPlayer;
import com.tty.mapper.BanPlayerMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BanPlayerManager extends BaseManager<BanPlayer> {

    public BanPlayerManager(boolean isAsync) {
        super(isAsync);
    }

    public CompletableFuture<BanPlayer> getInstance(String uuid) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
               BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
               return mapper.selectOne(new LambdaQueryWrapper<BanPlayer>().eq(BanPlayer::getPlayerUUID, uuid));
           }
        });
    }

    @Override
    public CompletableFuture<List<BanPlayer>> getList(int pageNum, int pageSize) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> createInstance(BanPlayer instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
               return mapper.insert(instance) == 1;
           }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteInstance(BanPlayer instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
               return mapper.delete(new LambdaQueryWrapper<BanPlayer>().eq(BanPlayer::getPlayerUUID, instance.getPlayerUUID())) == 1;
           }
        });
    }

    @Override
    public CompletableFuture<Boolean> modify(BanPlayer instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
                return mapper.update(instance, new LambdaQueryWrapper<BanPlayer>().eq(BanPlayer::getPlayerUUID, instance.getPlayerUUID())) == 1;
            }
        });
    }
}
