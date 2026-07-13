package com.tty.ari.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.ari.Ari;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.ari.mapper.WhitelistMapper;

import java.util.concurrent.CompletableFuture;

public class WhitelistManager extends BaseDataManager<WhitelistInstance> {

    public WhitelistManager(boolean isAsync) {
        super(() -> Ari.SQL_INSTANCE.getFactory(), isAsync);
    }

    @Override
    public CompletableFuture<PageResult<WhitelistInstance>> getList(int pageNum, int pageSize, LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTask(session -> {
            Page<WhitelistInstance> resultPage = session.getMapper(WhitelistMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
            return PageResult.build(
                    resultPage.getRecords(),
                    resultPage.getTotal(),
                    resultPage.getPages(),
                    resultPage.getCurrent());
        });
    }

    @Override
    public CompletableFuture<WhitelistInstance> get(LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTask(session -> session.getMapper(WhitelistMapper.class).selectOne(key));
    }

    @Override
    public CompletableFuture<WhitelistInstance> create(WhitelistInstance instance) {
        return this.executeTask(session -> session.getMapper(WhitelistMapper.class).insert(instance) == 1 ? instance:null);
    }

    @Override
    public CompletableFuture<Boolean> delete(WhitelistInstance entity) {
        return this.executeTransaction(session -> session.getMapper(WhitelistMapper.class).deleteById(entity) == 1);
    }

    @Override
    public CompletableFuture<Integer> delete(LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTransaction(session -> session.getMapper(WhitelistMapper.class).delete(key));
    }

    @Override
    public CompletableFuture<Boolean> update(WhitelistInstance instance, LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTransaction(session -> session.getMapper(WhitelistMapper.class).update(instance, key) == 1);
    }

}
