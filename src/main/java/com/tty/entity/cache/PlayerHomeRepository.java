package com.tty.entity.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.entity.ServerHome;
import com.tty.api.dto.PageResult;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.BaseDataManager;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PlayerHomeRepository extends EntityRepository<ServerHome> {

    public PlayerHomeRepository(BaseDataManager<ServerHome> manager) {
        super(manager);
    }

    @Override
    protected @Nullable Object resolvePartitionKey(ServerHome entity) {
        return entity.getPlayerUUID();
    }

    @Override
    protected @Nullable Object resolvePartitionKey(LambdaQueryWrapper<ServerHome> wrapper) {

        Ari.LOG.debug(wrapper.getCustomSqlSegment());
        Ari.LOG.debug(wrapper.getParamNameValuePairs().toString());
        return null;
    }

    public CompletableFuture<PageResult<ServerHome>> queryCount(LambdaQueryWrapper<ServerHome> queryKey) {
        return this.manager.getList(1, Integer.MAX_VALUE, queryKey);
    }

}
