package com.tty.ari.dto;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.utils.PublicFunctionUtils;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BungeeCache {

    private final AbstractJavaPlugin plugin;

    @Getter
    private volatile Set<String> servers = Set.of();
    @Getter
    private volatile State state = State.UNKNOWN;

    private CompletableFuture<Set<String>> pendingFuture = null;

    public BungeeCache(AbstractJavaPlugin plugin) {
        this.plugin = plugin;
    }

    public enum State {
        UNKNOWN,
        LOADING,
        READY
    }

    public CompletableFuture<Set<String>> getServers(String prefix) {
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(prefix, this.servers));
    }

    public synchronized CompletableFuture<Set<String>> waitForLoad(int timeoutSeconds) {
        if (this.state == State.READY) {
            return CompletableFuture.completedFuture(this.servers);
        }
        if (this.pendingFuture != null && !this.pendingFuture.isDone()) {
            return this.pendingFuture;
        }

        CompletableFuture<Set<String>> future = new CompletableFuture<>();
        this.pendingFuture = future;

        CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS, this.plugin.getExecutorAsync()).execute(() -> {
            synchronized (BungeeCache.class) {
                if (!future.isDone()) {
                    future.complete(Set.of());
                    this.state = State.UNKNOWN;
                    if (this.pendingFuture == future) {
                        this.pendingFuture = null;
                    }
                }
            }
        });

        return future;
    }

    public synchronized void onServersLoaded(Set<String> newServers) {
        servers = newServers;
        state = State.READY;
        if (pendingFuture != null && !pendingFuture.isDone()) {
            pendingFuture.complete(newServers);
            pendingFuture = null;
        }
    }

    public CompletableFuture<Set<String>> waitForLoad(int timeoutSeconds, Runnable triggerLoad) {
        synchronized (BungeeCache.class) {
            if (this.state == State.READY) {
                return CompletableFuture.completedFuture(this.servers);
            }

            if (this.state == State.UNKNOWN) {
                this.state = State.LOADING;

                if (triggerLoad != null) {
                    try {
                        triggerLoad.run();
                    } catch (Exception e) {
                        this.state = State.UNKNOWN;
                        CompletableFuture<Set<String>> failed = new CompletableFuture<>();
                        failed.completeExceptionally(e);
                        return failed;
                    }
                }
            }

            if (this.pendingFuture != null && !this.pendingFuture.isDone()) {
                return this.pendingFuture;
            }

            CompletableFuture<Set<String>> future = new CompletableFuture<>();
            this.pendingFuture = future;

            CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS, this.plugin.getExecutorAsync())
                    .execute(() -> {
                        synchronized (BungeeCache.class) {
                            if (!future.isDone()) {
                                future.complete(Set.of());
                                this.state = State.UNKNOWN;
                                if (this.pendingFuture == future) {
                                    this.pendingFuture = null;
                                }
                            }
                        }
                    });
            return future;
        }
    }

    public void shutdown() {
        this.servers = null;
        if (this.pendingFuture != null) {
            this.pendingFuture.cancel(true);
        }
        this.state = State.UNKNOWN;
    }

}