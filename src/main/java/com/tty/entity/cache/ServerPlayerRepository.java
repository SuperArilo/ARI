package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerPlayer;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class ServerPlayerRepository extends EntityRepository<ServerPlayer> {

    public ServerPlayerRepository(BaseDataManager<ServerPlayer> manager) {
        super(manager);
    }

    @Override
    protected @NotNull LambdaQueryWrapper<ServerPlayer> extractCacheKey(ServerPlayer entity) {
        return new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, entity.getPlayerUUID());
    }

    @Override
    protected LambdaQueryWrapper<ServerPlayer> extractPageQueryKey(ServerPlayer entity) {
        return null;
    }
}
