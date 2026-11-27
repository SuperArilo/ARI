package com.tty.ari.tool;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.Log;
import com.tty.api.enumType.SQLType;
import com.tty.api.persistence.CachedRepository;
import com.tty.api.persistence.model.EntityModel;
import com.tty.api.persistence.runtime.RepositoryRegistry;
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
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class RepositoryManager implements AutoCloseable {

    private final RepositoryRegistry registry;
    private final CachedRepository<ServerHome, Long> homes;
    private final CachedRepository<ServerWarp, Long> warps;
    private final CachedRepository<ServerPlayer, Integer> players;
    private final CachedRepository<BanPlayer, Long> bans;
    private final CachedRepository<WhitelistInstance, Long> whitelist;

    public RepositoryManager(AbstractJavaPlugin plugin, SQLInstance sql) {
        Log log = plugin.getLog();

        this.registry = new RepositoryRegistry(log, "ari-db", 4, Duration.ofSeconds(10));

        Executor executor = this.registry.executor();
        Supplier<SqlSessionFactory> factory = sql::getFactory;
        SQLType sqlType = sql.getSqlType();

        this.homes = this.registry.register(RepositoryRegistry.create(log,
                EntityModel.grouped(ServerHome.class, ServerHome::getId, ServerHome::getPlayerUUID),
                HomeMapper.class, factory, executor, sqlType));
        this.warps = this.registry.register(RepositoryRegistry.create(log,
                EntityModel.shared(ServerWarp.class, ServerWarp::getId),
                WarpMapper.class, factory, executor, sqlType));
        this.players = this.registry.register(RepositoryRegistry.create(log,
                EntityModel.shared(ServerPlayer.class, ServerPlayer::getId),
                PlayersMapper.class, factory, executor, sqlType));
        this.bans = this.registry.register(RepositoryRegistry.create(log,
                EntityModel.shared(BanPlayer.class, BanPlayer::getId),
                BanPlayerMapper.class, factory, executor, sqlType));
        this.whitelist = this.registry.register(RepositoryRegistry.create(log,
                EntityModel.shared(WhitelistInstance.class, WhitelistInstance::getId),
                WhitelistMapper.class, factory, executor, sqlType));
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
        this.registry.clearAllCaches();
    }

    @Override
    public void close() {
        this.registry.close();
    }

}
