package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.WhitelistInstance;
import com.tty.lib.dto.PageResult;
import com.tty.lib.tool.BaseDataManager;
import com.tty.mapper.WhitelistMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager extends BaseDataManager<WhitelistManager.QueryKey, WhitelistInstance> {

    public WhitelistManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<WhitelistInstance>> getList(int pageNum, int pageSize, WhitelistManager.QueryKey queryKey) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                Page<WhitelistInstance> page = new Page<>(pageNum, pageSize);
                Page<WhitelistInstance> resultPage = mapper.selectPage(page, new LambdaQueryWrapper<>());

                return PageResult.build(
                        resultPage.getRecords(),
                        resultPage.getTotal(),
                        resultPage.getPages(),
                        resultPage.getCurrent());
            }
        });
    }

    @Override
    public CompletableFuture<WhitelistInstance> getInstance(WhitelistManager.QueryKey queryKey) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                LambdaQueryWrapper<WhitelistInstance> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WhitelistInstance::getPlayerUUID, queryKey.playerUUID);
                return mapper.selectOne(wrapper);
            }
        });
    }

    @Override
    public CompletableFuture<WhitelistInstance> createInstance(WhitelistInstance instance) {
        return this.executeTask(() -> {
           try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
               WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
               return mapper.insert(instance) == 1 ? instance:null;
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

    public CompletableFuture<Integer> deleteInstances(List<String> uuids) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                WhitelistMapper mapper = session.getMapper(WhitelistMapper.class);
                return mapper.delete(new LambdaQueryWrapper<WhitelistInstance>().in(WhitelistInstance::getPlayerUUID, uuids));
            }
        });
    }

    public record QueryKey(String playerUUID) {}

}
