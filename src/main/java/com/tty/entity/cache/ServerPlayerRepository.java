package com.tty.entity.cache;

import com.tty.api.BaseJavaPlugin;
import com.tty.entity.ServerPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

public class ServerPlayerRepository extends EntityRepository<ServerPlayer> {

    public ServerPlayerRepository(BaseJavaPlugin plugin, BaseDataManager<ServerPlayer> manager) {
        super(plugin, manager);
    }

}
