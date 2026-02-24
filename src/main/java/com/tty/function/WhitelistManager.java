package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.WhitelistInstance;
import com.tty.api.dto.PageResult;
import com.tty.api.utils.BaseDataManager;
import com.tty.mapper.WhitelistMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.CompletableFuture;

public class WhitelistManager extends BaseDataManager<WhitelistInstance> {

    public WhitelistManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<WhitelistInstance>> getList(int pageNum, int pageSize, LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                Page<WhitelistInstance> resultPage = session.getMapper(WhitelistMapper.class).selectPage(new Page<>(pageNum, pageSize), key);
                return PageResult.build(
                        resultPage.getRecords(),
                        resultPage.getTotal(),
                        resultPage.getPages(),
                        resultPage.getCurrent());
            }
        });
    }

    @Override
    public CompletableFuture<WhitelistInstance> get(LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(WhitelistMapper.class).selectOne(key);
            }
        });
    }

    @Override
    public CompletableFuture<WhitelistInstance> create(WhitelistInstance instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               return session.getMapper(WhitelistMapper.class).insert(instance) == 1 ? instance:null;
           }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(WhitelistInstance entity) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(WhitelistMapper.class).deleteById(entity) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(LambdaQueryWrapper<WhitelistInstance> key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                return session.getMapper(WhitelistMapper.class).delete(key) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> update(WhitelistInstance instance, LambdaQueryWrapper<WhitelistInstance> key) {
        return null;
    }

}
