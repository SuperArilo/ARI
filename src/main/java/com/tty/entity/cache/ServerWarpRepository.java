package com.tty.entity.cache;

import com.tty.entity.ServerWarp;
import com.tty.function.WarpManager;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class ServerWarpRepository extends EntityRepository<WarpManager.QueryKey, ServerWarp> {

    public ServerWarpRepository(BaseDataManager<WarpManager.QueryKey, ServerWarp> manager) {
        super(manager);
    }

    @Override
    protected WarpManager.@NotNull QueryKey extractCacheKey(ServerWarp entity) {
        return new WarpManager.QueryKey(entity.getWarpId(), entity.getCreateBy());
    }

    @Override
    protected WarpManager.QueryKey extractPageQueryKey(ServerWarp entity) {
        return new WarpManager.QueryKey(null, null);
    }
}
