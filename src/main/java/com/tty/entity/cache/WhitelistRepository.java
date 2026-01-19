package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.WhitelistInstance;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class WhitelistRepository extends EntityRepository<WhitelistInstance> {

    public WhitelistRepository(BaseDataManager<WhitelistInstance> manager) {
        super(manager);
    }

    @Override
    protected @NotNull LambdaQueryWrapper<WhitelistInstance> extractCacheKey(WhitelistInstance entity) {
        return new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, entity.getPlayerUUID());
    }

    @Override
    protected LambdaQueryWrapper<WhitelistInstance> extractPageQueryKey(WhitelistInstance entity) {
        return new LambdaQueryWrapper<>(WhitelistInstance.class);
    }
}
