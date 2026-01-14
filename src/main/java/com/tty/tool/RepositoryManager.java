package com.tty.tool;
import com.tty.lib.dto.RepositoryException;
import com.tty.lib.services.EntityRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RepositoryManager {

    /**
     * 实体类型 -> Repository
     */
    private final Map<Class<?>, EntityRepository<?, ?>> repositories =  new ConcurrentHashMap<>();

    /**
     * 注册 Repository
     * 一个实体类型只能注册一个 Repository
     */
    public <K, T> void register(
            Class<T> entityClass,
            EntityRepository<K, T> repository
    ) {
        EntityRepository<?, ?> old =
                repositories.putIfAbsent(entityClass, repository);

        if (old != null) {
            throw new RepositoryException(
                    "Repository already registered for entity: "
                            + entityClass.getName()
            );
        }
    }

    /**
     * 获取 Repository（类型安全）
     */
    @SuppressWarnings("unchecked")
    public <K, T> EntityRepository<K, T> get(Class<T> entityClass) {
        EntityRepository<?, ?> repository = repositories.get(entityClass);
        if (repository == null) {
            throw new RepositoryException(
                    "No repository registered for entity: "
                            + entityClass.getName()
            );
        }
        return (EntityRepository<K, T>) repository;
    }

    /**
     * 是否已注册
     */
    public boolean isRegistered(Class<?> entityClass) {
        return repositories.containsKey(entityClass);
    }

    /**
     * 清空所有缓存（不影响 DB）
     */
    public void clearAllCache() {
        for (EntityRepository<?, ?> repository : repositories.values()) {
            repository.clearAllCache();
        }
    }

    /**
     * 框架关闭时调用
     */
    public void shutdown() {
        for (EntityRepository<?, ?> repository : repositories.values()) {
            repository.clearAllCache();
        }
        repositories.clear();
    }
}

