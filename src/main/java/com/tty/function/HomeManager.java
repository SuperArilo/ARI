package com.tty.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tty.entity.ServerHome;
import com.tty.mapper.HomeMapper;
import com.tty.tool.SQLInstance;
import org.apache.ibatis.session.SqlSession;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HomeManager extends BaseManager<ServerHome> {

    private final Player player;

    public HomeManager(Player player, boolean isAsync) {
        super(isAsync);
        this.player = player;
    }

    @Override
    public CompletableFuture<List<ServerHome>> getList(int pageNum, int pageSize) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.selectList(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<ServerHome>().eq(ServerHome::getPlayerUUID, this.player.getUniqueId().toString()).orderByDesc(ServerHome::isTopSlot));
            }
        });
    }

    public CompletableFuture<ServerHome> getInstance(String homeId) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession()) {

                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.selectOne(new LambdaQueryWrapper<ServerHome>().eq(ServerHome::getHomeId, homeId).eq(ServerHome::getPlayerUUID, this.player.getUniqueId().toString()));
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createInstance(ServerHome instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.insert(instance) == 1;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteInstance(ServerHome instance) {
        return this.executeTask(() -> {
            try (SqlSession session = SQLInstance.SESSION_FACTORY.openSession(true)) {
                HomeMapper mapper = session.getMapper(HomeMapper.class);
                return mapper.deleteById(instance) == 1;
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
}
