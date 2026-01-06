package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.WhitelistInstance;
import com.tty.mapper.WhitelistMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager extends BaseManager<WhitelistInstance> {

    public WhitelistManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<List<WhitelistInstance>> getList(int pageNum, int pageSize) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                return mapper.selectList(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<>());
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createInstance(WhitelistInstance instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
               return mapper.insert(instance) == 1;
           }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteInstance(WhitelistInstance instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                return mapper.deleteById(instance) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> modify(WhitelistInstance instance) {
        return null;
    }

    public CompletableFuture<WhitelistInstance> getInstance(String uuid) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                LambdaQueryWrapper<WhitelistInstance> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WhitelistInstance::getPlayerUUID, uuid);
                return mapper.selectOne(wrapper);
            }
        });
    }

    public CompletableFuture<Integer> deleteInstances(List<String> uuids) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                return mapper.delete(new LambdaQueryWrapper<WhitelistInstance>().in(WhitelistInstance::getPlayerUUID, uuids));
            }
        });
    }

}
