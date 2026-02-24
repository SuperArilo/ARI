package com.tty.entity.cache;

import com.tty.entity.WhitelistInstance;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

public class WhitelistRepository extends EntityRepository<WhitelistInstance> {

    public WhitelistRepository(BaseDataManager<WhitelistInstance> manager) {
        super(manager);
    }

}
