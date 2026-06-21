package com.tty.ari.dto;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.utils.PublicFunctionUtils;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BungeeCache {

    private final Object lock = new Object();

    private final AbstractJavaPlugin plugin;
    @Getter
    private volatile Set<String> servers;

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

    public void onServersLoaded(Set<String> newServers) {
        CompletableFuture<Set<String>> toComplete = null;
        synchronized (this.lock) {
            this.servers = newServers;
            this.state = State.READY;
            if (this.pendingFuture != null && !this.pendingFuture.isDone()) {
                toComplete = pendingFuture;
                this.pendingFuture = null;
            }
        }
        if (toComplete != null) {
            toComplete.complete(newServers);
        }
    }

    public CompletableFuture<Set<String>> waitForLoad(int timeoutSeconds, Runnable runnable) {
        synchronized (this.lock) {
            if (this.state == State.READY) {
                return CompletableFuture.completedFuture(this.servers);
            }
            if (this.state == State.LOADING) {
                if (this.pendingFuture != null && !this.pendingFuture.isDone()) {
                    return this.pendingFuture;
                }
            }
            this.state = State.LOADING;
            CompletableFuture<Set<String>> future = new CompletableFuture<>();
            this.pendingFuture = future;

            CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS, this.plugin.getExecutorAsync()).execute(() -> {
                synchronized (this.lock) {
                    if (!future.isDone()) {
                        future.complete(Set.of());
                        this.state = State.UNKNOWN;
                        if (this.pendingFuture == future) {
                            this.pendingFuture = null;
                        }
                    }
                }
            });
            if (runnable != null) {
                this.plugin.getExecutorAsync().execute(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        synchronized (this.lock) {
                            if (!future.isDone()) {
                                future.completeExceptionally(e);
                                this.state = State.UNKNOWN;
                                if (this.pendingFuture == future) {
                                    this.pendingFuture = null;
                                }
                            }
                        }
                    }
                });
            }
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