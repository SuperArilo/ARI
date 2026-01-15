package com.tty.entity.cache;

import com.tty.entity.ServerHome;
import com.tty.function.HomeManager;
import com.tty.lib.dto.PageResult;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PlayerHomeRepository extends EntityRepository<HomeManager.QueryKey, ServerHome> {

    public PlayerHomeRepository(BaseDataManager<HomeManager.QueryKey, ServerHome> manager) {
        super(manager);
    }

    @Override
    protected HomeManager.@NotNull QueryKey extractCacheKey(ServerHome entity) {
        return new HomeManager.QueryKey(entity.getPlayerUUID(), entity.getHomeId());
    }

    @Override
    protected HomeManager.QueryKey extractPageQueryKey(ServerHome entity) {
        return new HomeManager.QueryKey(entity.getPlayerUUID(), null);
    }

    public CompletableFuture<PageResult<ServerHome>> queryCount(HomeManager.QueryKey queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
