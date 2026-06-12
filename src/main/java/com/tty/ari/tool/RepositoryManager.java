package com.tty.ari.tool;
import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.entity.*;
import com.tty.ari.entity.cache.*;
import com.tty.ari.function.*;
import com.tty.api.repository.EntityRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RepositoryManager {

    private final Map<Class<?>, EntityRepository<?>> repositories =  new ConcurrentHashMap<>();
    private final AbstractJavaPlugin plugin;

    public RepositoryManager(AbstractJavaPlugin plugin) {
        this.plugin = plugin;
        this.init();
    }

    private void init() {
        this.register(ServerHome.class, new PlayerHomeRepository(this.plugin, new HomeManager(true)));
        this.register(BanPlayer.class, new BanPlayerRepository(this.plugin, new BanPlayerManager(true)));
        this.register(ServerPlayer.class, new ServerPlayerRepository(this.plugin, new PlayerManager(true)));
        this.register(ServerWarp.class, new ServerWarpRepository(this.plugin, new WarpManager(true)));
        this.register(WhitelistInstance.class, new WhitelistRepository(this.plugin, new WhitelistManager(true)));
    }

    public <T> void register(Class<T> entityClass, EntityRepository<T> repository) {
        EntityRepository<?> old = this.repositories.putIfAbsent(entityClass, repository);
        if (old != null) {
            throw new IllegalArgumentException("Repository already registered for entity: " + entityClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> EntityRepository<T> get(Class<T> entityClass) {
        EntityRepository<?> repository = this.repositories.get(entityClass);
        if (repository == null) {
            throw new IllegalArgumentException("No repository registered for entity: " + entityClass.getName());
        }
        return (EntityRepository<T>) repository;
    }

    public boolean isRegistered(Class<?> entityClass) {
        return this.repositories.containsKey(entityClass);
    }

    public void clearAllCache() {
        for (EntityRepository<?> repository : this.repositories.values()) {
            repository.clearAllCache();
        }
    }

    public void stop() {
        this.repositories.forEach((k, v) -> v.shutdown());
    }

}

