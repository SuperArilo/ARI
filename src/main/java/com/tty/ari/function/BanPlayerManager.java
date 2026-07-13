package com.tty.ari.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.ari.Ari;
import com.tty.ari.entity.BanPlayer;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.ari.mapper.BanPlayerMapper;

import java.util.concurrent.CompletableFuture;

public class BanPlayerManager extends BaseDataManager<BanPlayer> {

    public BanPlayerManager(boolean isAsync) {
        super(() -> Ari.SQL_INSTANCE.getFactory(), isAsync);
    }

    @Override
    public CompletableFuture<PageResult<BanPlayer>> getList(int pageNum, int pageSize, LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTask(session -> {
            Page<BanPlayer> resultPage = session.getMapper(BanPlayerMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
            return PageResult.build(
                    resultPage.getRecords(),
                    resultPage.getTotal(),
                    resultPage.getPages(),
                    resultPage.getCurrent());
        });
    }

    @Override
    public CompletableFuture<BanPlayer> get(LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTask(session -> session.getMapper(BanPlayerMapper.class).selectOne(key));
    }

    @Override
    public CompletableFuture<BanPlayer> create(BanPlayer instance) {
        return this.executeTask(session -> session.getMapper(BanPlayerMapper.class).insert(instance) == 1 ? instance:null);
    }

    @Override
    public CompletableFuture<Boolean> delete(BanPlayer entity) {
        return this.executeTransaction(session -> session.getMapper(BanPlayerMapper.class).deleteById(entity) == 1);
    }

    @Override
    public CompletableFuture<Integer> delete(LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTransaction(session -> session.getMapper(BanPlayerMapper.class).delete(key));
    }

    @Override
    public CompletableFuture<Boolean> update(BanPlayer instance, LambdaQueryWrapper<BanPlayer> key) {
        return this.executeTransaction(session -> session.getMapper(BanPlayerMapper.class).update(instance, key) == 1);
    }

}
