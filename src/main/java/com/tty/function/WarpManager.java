package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.ServerWarp;
import com.tty.mapper.WarpMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpManager extends BaseManager<ServerWarp> {

    public WarpManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<List<ServerWarp>> getList(int pageNum, int pageSize) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                Page<ServerWarp> page = new Page<>(pageNum, pageSize);
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.selectList(page, new LambdaQueryWrapper<ServerWarp>().orderByDesc(ServerWarp::isTopSlot));
            }
        });
    }

    public CompletableFuture<List<ServerWarp>> getCountByPlayer(String uuid) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.selectList(new Page<>(0, Integer.MAX_VALUE), new LambdaQueryWrapper<ServerWarp>().eq(ServerWarp::getCreateBy, uuid));
            }
        });
    }

    public CompletableFuture<ServerWarp> getInstance(String warpId) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.selectOne(new LambdaQueryWrapper<ServerWarp>().eq(ServerWarp::getWarpId, warpId));
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createInstance(ServerWarp instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.insert(instance) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteInstance(ServerWarp instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.delete(new LambdaQueryWrapper<ServerWarp>().eq(ServerWarp::getCreateBy, instance.getCreateBy()).eq(ServerWarp::getId, instance.getId())) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> modify(ServerWarp instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.update(instance, new LambdaQueryWrapper<ServerWarp>().eq(ServerWarp::getId, instance.getId()).eq(ServerWarp::getCreateBy, instance.getCreateBy())) == 1;
            }
        });
    }
}
