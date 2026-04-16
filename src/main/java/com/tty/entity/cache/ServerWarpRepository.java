package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.BaseJavaPlugin;
import com.tty.entity.ServerWarp;
import com.tty.api.dto.PageResult;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

import java.util.concurrent.CompletableFuture;

public class ServerWarpRepository extends EntityRepository<ServerWarp> {

    public ServerWarpRepository(BaseJavaPlugin plugin, BaseDataManager<ServerWarp> manager) {
        super(plugin, manager);
    }

    public CompletableFuture<PageResult<ServerWarp>> queryCount(LambdaQueryWrapper<ServerWarp> queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
