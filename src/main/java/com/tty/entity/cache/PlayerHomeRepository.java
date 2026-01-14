package com.tty.entity.cache;

import com.tty.entity.ServerHome;
import com.tty.function.HomeManager;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;

public class PlayerHomeRepository extends EntityRepository<HomeManager.QueryKey, ServerHome> {

    public PlayerHomeRepository(BaseDataManager<HomeManager.QueryKey, ServerHome> manager) {
        super(manager);
    }

    @Override
    protected HomeManager.QueryKey extractCacheKey(ServerHome entity) {
        return new HomeManager.QueryKey(entity.getPlayerUUID(), entity.getHomeId());
    }

    @Override
    protected HomeManager.QueryKey extractPageQueryKey(ServerHome entity) {
        return new HomeManager.QueryKey(entity.getPlayerUUID(), null);
    }

}
