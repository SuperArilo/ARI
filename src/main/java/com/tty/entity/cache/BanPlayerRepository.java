package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.BanPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class BanPlayerRepository extends EntityRepository<BanPlayer> {

    public BanPlayerRepository(BaseDataManager< BanPlayer> manager) {
        super(manager);
    }

    @Override
    protected @NotNull LambdaQueryWrapper<BanPlayer> extractCacheKey(BanPlayer entity) {
        return new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, entity.getPlayerUUID());
    }

    @Override
    protected LambdaQueryWrapper<BanPlayer> extractPageQueryKey(BanPlayer entity) {
        return null;
    }
}
