package com.tty.ari.entity.cache;

import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.entity.BanPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

public class BanPlayerRepository extends EntityRepository<BanPlayer> {

    public BanPlayerRepository(AbstractJavaPlugin plugin, BaseDataManager< BanPlayer> manager) {
        super(plugin, manager);
    }

}
