package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerWarp;
import com.tty.api.dto.PageResult;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ServerWarpRepository extends EntityRepository<ServerWarp> {

    public ServerWarpRepository(BaseDataManager<ServerWarp> manager) {
        super(manager);
    }

    @Override
    protected @NotNull LambdaQueryWrapper<ServerWarp> extractCacheKey(ServerWarp entity) {
        return new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, entity.getWarpId()).eq(ServerWarp::getCreateBy, entity.getCreateBy());
    }

    @Override
    protected LambdaQueryWrapper<ServerWarp> extractPageQueryKey(ServerWarp entity) {
        return new LambdaQueryWrapper<>(ServerWarp.class);
    }

    public CompletableFuture<PageResult<ServerWarp>> queryCount(LambdaQueryWrapper<ServerWarp> queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
