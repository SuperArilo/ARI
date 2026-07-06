package com.tty.ari.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.ari.Ari;
import com.tty.ari.entity.ServerPlayer;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.ari.mapper.PlayersMapper;

import java.util.concurrent.CompletableFuture;

public class PlayerManager extends BaseDataManager<ServerPlayer> {

    public PlayerManager(boolean isAsync) {
        super(Ari.SQL_INSTANCE.getFactory(), isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerPlayer>> getList(int pageNum, int pageSize, LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTask(session -> {
            Page<ServerPlayer> resultPage = session.getMapper(PlayersMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
            return PageResult.build(
                    resultPage.getRecords(),
                    resultPage.getTotal(),
                    resultPage.getPages(),
                    resultPage.getCurrent());
        });
    }

    @Override
    public CompletableFuture<ServerPlayer> get(LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTask(session -> session.getMapper(PlayersMapper.class).selectOne(key));
    }

    @Override
    public CompletableFuture<ServerPlayer> create(ServerPlayer instance) {
        return this.executeTask(session -> session.getMapper(PlayersMapper.class).insert(instance) == 1 ? instance:null);
    }

    @Override
    public CompletableFuture<Boolean> delete(ServerPlayer entity) {
        return this.executeTransaction(session -> session.getMapper(PlayersMapper.class).deleteById(entity) == 1);
    }

    @Override
    public CompletableFuture<Integer> delete(LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTransaction(session -> session.getMapper(PlayersMapper.class).delete(key));
    }

    @Override
    public CompletableFuture<Boolean> update(ServerPlayer instance, LambdaQueryWrapper<ServerPlayer> key) {
        return this.executeTransaction(session -> session.getMapper(PlayersMapper.class).update(instance, key) == 1);
    }

}
