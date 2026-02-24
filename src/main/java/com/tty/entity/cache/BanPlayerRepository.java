package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.BanPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.Nullable;

public class BanPlayerRepository extends EntityRepository<BanPlayer> {

    public BanPlayerRepository(BaseDataManager< BanPlayer> manager) {
        super(manager);
    }

    @Override
    protected @Nullable Object resolvePartitionKey(BanPlayer entity) {
        return entity.getPlayerUUID();
    }

    @Override
    protected @Nullable Object resolvePartitionKey(LambdaQueryWrapper<BanPlayer> wrapper) {
        return null;
    }

}
