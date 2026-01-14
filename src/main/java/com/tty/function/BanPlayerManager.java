package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.BanPlayer;
import com.tty.lib.dto.PageResult;
import com.tty.lib.tool.BaseDataManager;
import com.tty.mapper.BanPlayerMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.CompletableFuture;

public class BanPlayerManager extends BaseDataManager<BanPlayerManager.QueryKey, BanPlayer> {

    public BanPlayerManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<BanPlayer>> getList(int pageNum, int pageSize, BanPlayerManager.QueryKey queryKey) {
        return null;
    }

    @Override
    public CompletableFuture<BanPlayer> getInstance(BanPlayerManager.QueryKey queryKey) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
                return mapper.selectOne(new LambdaQueryWrapper<BanPlayer>().eq(BanPlayer::getPlayerUUID, queryKey.playerUUID));
            }
        });
    }

    @Override
    public CompletableFuture<BanPlayer> createInstance(BanPlayer instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
               return mapper.insert(instance) == 1 ? instance:null;
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

    public record QueryKey(String playerUUID) {}
}
