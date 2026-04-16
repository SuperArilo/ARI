package com.tty.entity.cache;

import com.tty.api.BaseJavaPlugin;
import com.tty.entity.BanPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

public class BanPlayerRepository extends EntityRepository<BanPlayer> {

    public BanPlayerRepository(BaseJavaPlugin plugin, BaseDataManager< BanPlayer> manager) {
        super(plugin, manager);
    }

}
