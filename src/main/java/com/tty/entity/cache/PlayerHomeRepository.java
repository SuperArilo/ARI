package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerHome;
import com.tty.api.dto.PageResult;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PlayerHomeRepository extends EntityRepository<ServerHome> {

    public PlayerHomeRepository(BaseDataManager<ServerHome> manager) {
        super(manager);
    }

    @Override
    protected @NotNull LambdaQueryWrapper<ServerHome> extractCacheKey(ServerHome entity) {
        return new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getPlayerUUID, entity.getPlayerUUID()).eq(ServerHome::getHomeId, entity.getHomeId());
    }

    @Override
    protected LambdaQueryWrapper<ServerHome> extractPageQueryKey(ServerHome entity) {
        return new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getPlayerUUID, entity.getPlayerUUID());
    }

    public CompletableFuture<PageResult<ServerHome>> queryCount(LambdaQueryWrapper<ServerHome> queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
