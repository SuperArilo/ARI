package com.tty.ari.entity.cache;

import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.entity.ServerPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

public class ServerPlayerRepository extends EntityRepository<ServerPlayer> {

    public ServerPlayerRepository(AbstractJavaPlugin plugin, BaseDataManager<ServerPlayer> manager) {
        super(plugin, manager);
    }

}
