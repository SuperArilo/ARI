package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.ServerWarp;
import com.tty.lib.dto.PageResult;
import com.tty.lib.tool.BaseDataManager;
import com.tty.mapper.WarpMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpManager extends BaseDataManager<WarpManager.QueryKey, ServerWarp> {

    public WarpManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerWarp>> getList(int pageNum, int pageSize, WarpManager.QueryKey queryKey) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                Page<ServerWarp> page = new Page<>(pageNum, pageSize);
                Page<ServerWarp> resultPage = mapper.selectPage(
                        page,
                        new LambdaQueryWrapper<ServerWarp>()
                                .orderByDesc(ServerWarp::isTopSlot)
                );
                return PageResult.build(
                        resultPage.getRecords(),
                        resultPage.getTotal(),
                        resultPage.getPages(),
                        resultPage.getCurrent());
            }
        });
    }

    @Override
    public CompletableFuture<ServerWarp> getInstance(WarpManager.QueryKey queryKey) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.selectOne(new LambdaQueryWrapper<ServerWarp>().eq(ServerWarp::getWarpId, queryKey.warpId));
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

    @Override
    public CompletableFuture<ServerWarp> createInstance(ServerWarp instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WarpMapper mapper = session.getMapper(WarpMapper.class);
                return mapper.insert(instance) == 1 ? instance:null;
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

    public record QueryKey(String warpId) {}

}
