package com.tty.entity.cache;

import com.tty.entity.ServerPlayer;
import com.tty.function.PlayerManager;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.BaseDataManager;
import org.jetbrains.annotations.NotNull;

public class ServerPlayerRepository extends EntityRepository<PlayerManager.QueryKey, ServerPlayer> {

    public ServerPlayerRepository(BaseDataManager<PlayerManager.QueryKey, ServerPlayer> manager) {
        super(manager);
    }

    @Override
    protected PlayerManager.@NotNull QueryKey extractCacheKey(ServerPlayer entity) {
        return new PlayerManager.QueryKey(entity.getPlayerUUID());
    }

    @Override
    protected PlayerManager.QueryKey extractPageQueryKey(ServerPlayer entity) {
        return null;
    }
}
