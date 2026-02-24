package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.WhitelistInstance;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.Nullable;

public class WhitelistRepository extends EntityRepository<WhitelistInstance> {

    public WhitelistRepository(BaseDataManager<WhitelistInstance> manager) {
        super(manager);
    }

    @Override
    protected @Nullable Object resolvePartitionKey(WhitelistInstance entity) {
        return null;
    }

    @Override
    protected @Nullable Object resolvePartitionKey(LambdaQueryWrapper<WhitelistInstance> wrapper) {
        return null;
    }

}
