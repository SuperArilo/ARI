package com.tty.entity.cache;

import com.tty.entity.BanPlayer;
import com.tty.function.BanPlayerManager;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class BanPlayerRepository extends EntityRepository<BanPlayerManager.QueryKey, BanPlayer> {

    public BanPlayerRepository(BaseDataManager<BanPlayerManager.QueryKey, BanPlayer> manager) {
        super(manager);
    }

    @Override
    protected BanPlayerManager.@NotNull QueryKey extractCacheKey(BanPlayer entity) {
        return new BanPlayerManager.QueryKey(entity.getPlayerUUID());
    }

    @Override
    protected BanPlayerManager.QueryKey extractPageQueryKey(BanPlayer entity) {
        return null;
    }
}
