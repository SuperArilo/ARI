package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.BaseJavaPlugin;
import com.tty.entity.ServerHome;
import com.tty.api.dto.PageResult;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;

import java.util.concurrent.CompletableFuture;

public class PlayerHomeRepository extends EntityRepository<ServerHome> {

    public PlayerHomeRepository(BaseJavaPlugin plugin, BaseDataManager<ServerHome> manager) {
        super(plugin, manager);
    }

    public CompletableFuture<PageResult<ServerHome>> queryCount(LambdaQueryWrapper<ServerHome> queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
