package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.entity.ServerPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlayerRepository extends EntityRepository<ServerPlayer> {

    public ServerPlayerRepository(BaseDataManager<ServerPlayer> manager) {
        super(manager);
    }

    @Override
    protected @Nullable Object resolvePartitionKey(ServerPlayer entity) {
        return null;
    }

    @Override
    protected @Nullable Object resolvePartitionKey(LambdaQueryWrapper<ServerPlayer> wrapper) {
        return null;
    }

}
