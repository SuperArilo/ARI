package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerWarp;
import com.tty.api.dto.PageResult;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ServerWarpRepository extends EntityRepository<ServerWarp> {

    public ServerWarpRepository(BaseDataManager<ServerWarp> manager) {
        super(manager);
    }

    @Override
    protected @Nullable Object resolvePartitionKey(ServerWarp entity) {
        return null;
    }

    @Override
    protected @Nullable Object resolvePartitionKey(LambdaQueryWrapper<ServerWarp> wrapper) {
        return null;
    }

    public CompletableFuture<PageResult<ServerWarp>> queryCount(LambdaQueryWrapper<ServerWarp> queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
