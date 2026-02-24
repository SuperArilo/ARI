package com.tty.entity.cache;

import com.tty.entity.ServerPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.Nullable;

public class ServerPlayerRepository extends EntityRepository<ServerPlayer> {

    public ServerPlayerRepository(BaseDataManager<ServerPlayer> manager) {
        super(manager);
    }

}
