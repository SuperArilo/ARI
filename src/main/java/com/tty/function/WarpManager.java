package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.ServerWarp;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.mapper.WarpMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpManager extends BaseDataManager<ServerWarp> {

    public WarpManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerWarp>> getList(int pageNum, int pageSize, LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                Page<ServerWarp> resultPage = session.getMapper(WarpMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
                return PageResult.build(
                        resultPage.getRecords(),
                        resultPage.getTotal(),
                        resultPage.getPages(),
                        resultPage.getCurrent());
            }
        });
    }

    @Override
    public CompletableFuture<ServerWarp> get(LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                return session.getMapper(WarpMapper.class).selectOne(key);
            }
        });
    }

    @Override
    public CompletableFuture<ServerWarp> create(ServerWarp instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(WarpMapper.class).insert(instance) == 1 ? instance:null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(ServerWarp entity) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> delete(LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(WarpMapper.class).delete(key) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> update(ServerWarp instance, LambdaQueryWrapper<ServerWarp> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(WarpMapper.class).update(instance, key) == 1;
            }
        });
    }

}
