package com.tty.ari.tool;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tty.api.AbstractJavaPlugin;
import com.tty.api.enumType.SQLType;
import com.tty.api.persistence.CachedRepository;
import com.tty.api.persistence.model.EntityModel;
import com.tty.api.persistence.mybatis.MyBatisEntityStore;
import com.tty.ari.entity.BanPlayer;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.entity.ServerWarp;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.mapper.BanPlayerMapper;
import com.tty.ari.mapper.HomeMapper;
import com.tty.ari.mapper.PlayersMapper;
import com.tty.ari.mapper.WarpMapper;
import com.tty.ari.mapper.WhitelistMapper;
import org.apache.ibatis.session.SqlSessionFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class RepositoryManager implements AutoCloseable {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

    private final AbstractJavaPlugin plugin;
    private final ExecutorService dbExecutor;

    private final CachedRepository<ServerHome, Long> homes;
    private final CachedRepository<ServerWarp, Long> warps;
    private final CachedRepository<ServerPlayer, Integer> players;
    private final CachedRepository<BanPlayer, Long> bans;
    private final CachedRepository<WhitelistInstance, Long> whitelist;

    public RepositoryManager(AbstractJavaPlugin plugin, SQLInstance sql) {
        this.plugin = plugin;
        this.dbExecutor = Executors.newFixedThreadPool(4, task -> {
            Thread thread = new Thread(task, "ari-db");
            thread.setDaemon(true);
            return thread;
        });

        Supplier<SqlSessionFactory> factory = sql::getFactory;
        SQLType sqlType = sql.getSqlType();

        this.homes = new CachedRepository<>(this.plugin.getLog(), this.store(EntityModel.grouped(ServerHome.class, ServerHome::getId, ServerHome::getPlayerUUID), HomeMapper.class, factory, sqlType));
        this.warps = new CachedRepository<>(this.plugin.getLog(), this.store(EntityModel.shared(ServerWarp.class, ServerWarp::getId), WarpMapper.class, factory, sqlType));
        this.players = new CachedRepository<>(this.plugin.getLog(), this.store(EntityModel.shared(ServerPlayer.class, ServerPlayer::getId), PlayersMapper.class, factory, sqlType));
        this.bans = new CachedRepository<>(this.plugin.getLog(), this.store(EntityModel.shared(BanPlayer.class, BanPlayer::getId), BanPlayerMapper.class, factory, sqlType));
        this.whitelist = new CachedRepository<>(this.plugin.getLog(), this.store(EntityModel.shared(WhitelistInstance.class, WhitelistInstance::getId), WhitelistMapper.class, factory, sqlType));
    }

    private <T, ID> MyBatisEntityStore<T, ID> store(EntityModel<T, ID> model, Class<? extends BaseMapper<T>> mapper, Supplier<SqlSessionFactory> factory, SQLType sqlType) {
        return new MyBatisEntityStore<>(model, factory, mapper, this.dbExecutor, sqlType);
    }

    public CachedRepository<ServerHome, Long> homes() {
        return this.homes;
    }

    public CachedRepository<ServerWarp, Long> warps() {
        return this.warps;
    }

    public CachedRepository<ServerPlayer, Integer> players() {
        return this.players;
    }

    public CachedRepository<BanPlayer, Long> bans() {
        return this.bans;
    }

    public CachedRepository<WhitelistInstance, Long> whitelist() {
        return this.whitelist;
    }

    public void clearAllCaches() {
        for (CachedRepository<?, ?> repository : this.repositories()) {
            repository.clearCache();
        }
    }

    private List<CachedRepository<?, ?>> repositories() {
        return List.of(this.homes, this.warps, this.players, this.bans, this.whitelist);
    }

    @Override
    public void close() {
        for (CachedRepository<?, ?> repository : this.repositories()) {
            try {
                repository.close(CLOSE_TIMEOUT);
            } catch (RuntimeException e) {
                this.plugin.getLog().error(e, "close repository did not finish in time");
            }
        }
        this.dbExecutor.shutdown();
    }

}
