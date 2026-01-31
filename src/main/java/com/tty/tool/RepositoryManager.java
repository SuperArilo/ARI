package com.tty.tool;
import com.tty.entity.*;
import com.tty.entity.cache.*;
import com.tty.function.*;
import com.tty.api.repository.EntityRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RepositoryManager {

    private final Map<Class<?>, EntityRepository<?>> repositories =  new ConcurrentHashMap<>();

    public RepositoryManager() {
        this.register(ServerHome.class, new PlayerHomeRepository(new HomeManager(true)));
        this.register(BanPlayer.class, new BanPlayerRepository(new BanPlayerManager(true)));
        this.register(ServerPlayer.class, new ServerPlayerRepository(new PlayerManager(true)));
        this.register(ServerWarp.class, new ServerWarpRepository(new WarpManager(true)));
        this.register(WhitelistInstance.class, new WhitelistRepository(new WhitelistManager(true)));
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

}

