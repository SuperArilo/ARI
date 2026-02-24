package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.ServerHome;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.mapper.HomeMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.CompletableFuture;

public class HomeManager extends BaseDataManager<ServerHome> {

    public HomeManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerHome>> getList(int pageNum, int pageSize , LambdaQueryWrapper<ServerHome> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                Page<ServerHome> resultPage = session.getMapper(HomeMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
                return PageResult.build(
                        resultPage.getRecords(),
                        resultPage.getTotal(),
                        resultPage.getPages(),
                        resultPage.getCurrent());
            }
        });
    }

    @Override
    public CompletableFuture<ServerHome> get(LambdaQueryWrapper<ServerHome> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                return session.getMapper(HomeMapper.class).selectOne(key);
            }
        });
    }

    @Override
    public CompletableFuture<ServerHome> create(ServerHome instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(HomeMapper.class).insert(instance) == 1 ? instance:null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(ServerHome entity) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(HomeMapper.class).deleteById(entity) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(LambdaQueryWrapper<ServerHome> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(HomeMapper.class).delete(key) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> update(ServerHome instance, LambdaQueryWrapper<ServerHome> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(HomeMapper.class).update(instance, key) == 1;
            }
        });
    }

}
