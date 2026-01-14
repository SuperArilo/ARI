package com.tty.entity.cache;

import com.tty.entity.WhitelistInstance;
import com.tty.function.WhitelistManager;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class WhitelistRepository extends EntityRepository<WhitelistManager.QueryKey, WhitelistInstance> {

    public WhitelistRepository(BaseDataManager<WhitelistManager.QueryKey, WhitelistInstance> manager) {
        super(manager);
    }

    @Override
    protected WhitelistManager.@NotNull QueryKey extractCacheKey(WhitelistInstance entity) {
        return new WhitelistManager.QueryKey(entity.getPlayerUUID());
    }

    @Override
    protected WhitelistManager.QueryKey extractPageQueryKey(WhitelistInstance entity) {
        return null;
    }
}
