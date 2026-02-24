package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.BanPlayer;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.mapper.BanPlayerMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.CompletableFuture;

public class BanPlayerManager extends BaseDataManager<BanPlayer> {

    public BanPlayerManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<BanPlayer>> getList(int pageNum, int pageSize, LambdaQueryWrapper<BanPlayer> key) {
        return null;
    }

    @Override
    public CompletableFuture<BanPlayer> get(LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                BanPlayerMapper mapper = session.getMapper(BanPlayerMapper.class);
                return mapper.selectOne(key);
            }
        });
    }

    @Override
    public CompletableFuture<BanPlayer> create(BanPlayer instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               return session.getMapper(BanPlayerMapper.class).insert(instance) == 1 ? instance:null;
           }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(BanPlayer entity) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(BanPlayerMapper.class).deleteById(entity) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               return session.getMapper(BanPlayerMapper.class).delete(key) == 1;
           }
        });
    }

    @Override
    public CompletableFuture<Boolean> update(BanPlayer instance, LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(BanPlayerMapper.class).update(instance, key) == 1;
            }
        });
    }

}
