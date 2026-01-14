package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.ServerHome;
import com.tty.lib.dto.PageResult;
import com.tty.lib.tool.BaseDataManager;
import com.tty.mapper.HomeMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.concurrent.CompletableFuture;

public class HomeManager extends BaseDataManager<HomeManager.QueryKey, ServerHome> {

    public HomeManager(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public CompletableFuture<PageResult<ServerHome>> getList(int pageNum, int pageSize , QueryKey key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                Page<ServerHome> page = new Page<>(pageNum, pageSize);
                Page<ServerHome> resultPage = mapper.selectPage(
                        page,
                        new LambdaQueryWrapper<ServerHome>()
                                .eq(ServerHome::getPlayerUUID, key.playerUUID)
                                .orderByDesc(ServerHome::isTopSlot)
                );
                return PageResult.build(
                        resultPage.getRecords(),
                        resultPage.getTotal(),
                        resultPage.getPages(),
                        resultPage.getCurrent());
            }
        });
    }

    @Override
    public CompletableFuture<ServerHome> getInstance(QueryKey key) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.selectOne(new LambdaQueryWrapper<ServerHome>().eq(ServerHome::getHomeId, key.homeId).eq(ServerHome::getPlayerUUID, key.playerUUID));
            }
        });
    }

    @Override
    public CompletableFuture<ServerHome> createInstance(ServerHome instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.insert(instance) == 1 ? instance:null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteInstance(ServerHome instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.delete(
                        new LambdaQueryWrapper<ServerHome>()
                                .eq(ServerHome::getPlayerUUID, instance.getPlayerUUID())
                                .eq(ServerHome::getHomeId, instance.getHomeId())
                ) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> modify(ServerHome instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.updateById(instance) == 1;
            }
        });
    }

    public record QueryKey(String playerUUID, String homeId) {}

}
