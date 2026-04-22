package com.tty.ari.entity.cache;

import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

public class WhitelistRepository extends EntityRepository<WhitelistInstance> {

    public WhitelistRepository(AbstractJavaPlugin plugin, BaseDataManager<WhitelistInstance> manager) {
        super(plugin, manager);
    }

}
