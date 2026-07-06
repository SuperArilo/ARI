package com.tty.ari.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.ari.Ari;
import com.tty.ari.entity.ServerWarp;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.ari.mapper.WarpMapper;

import java.util.concurrent.CompletableFuture;

public class WarpManager extends BaseDataManager<ServerWarp> {

    public WarpManager(boolean isAsync) {
        super(Ari.SQL_INSTANCE.getFactory(), isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerWarp>> getList(int pageNum, int pageSize, LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTask(session -> {
            Page<ServerWarp> resultPage = session.getMapper(WarpMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
            return PageResult.build(
                    resultPage.getRecords(),
                    resultPage.getTotal(),
                    resultPage.getPages(),
                    resultPage.getCurrent());
        });
    }

    @Override
    public CompletableFuture<ServerWarp> get(LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTask(session -> session.getMapper(WarpMapper.class).selectOne(key));
    }

    @Override
    public CompletableFuture<ServerWarp> create(ServerWarp instance) {
        return this.executeTask(session -> session.getMapper(WarpMapper.class).insert(instance) == 1 ? instance:null);
    }

    @Override
    public CompletableFuture<Boolean> delete(ServerWarp entity) {
        return this.executeTransaction(session -> session.getMapper(WarpMapper.class).deleteById(entity) == 1);
    }

    @Override
    public CompletableFuture<Integer> delete(LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTransaction(session -> session.getMapper(WarpMapper.class).delete(key));
    }

    @Override
    public CompletableFuture<Boolean> update(ServerWarp instance, LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTransaction(session -> session.getMapper(WarpMapper.class).update(instance, key) == 1);
    }

}
