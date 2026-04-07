package com.tty.dto;

import com.tty.api.utils.PublicFunctionUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BungeeCache {

    @Getter
    @Setter
    private static volatile Set<String> servers = Set.of();
    @Getter
    @Setter
    private static volatile State state = State.UNKNOWN;
    private static CompletableFuture<Set<String>> pendingFuture = null;

    public enum State {
        UNKNOWN,
        LOADING,
        READY
    }

    public static CompletableFuture<Set<String>> getServers(String prefix) {
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(prefix, servers));
    }

    public static synchronized CompletableFuture<Set<String>> waitForLoad(int timeoutSeconds) {
        if (state == State.READY) {
            return CompletableFuture.completedFuture(servers);
        }
        if (pendingFuture != null && !pendingFuture.isDone()) {
            return pendingFuture;
        }

        CompletableFuture<Set<String>> future = new CompletableFuture<>();
        pendingFuture = future;

        CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS)
                .execute(() -> {
                    synchronized (BungeeCache.class) {
                        if (!future.isDone()) {
                            future.complete(Set.of());
                            state = State.UNKNOWN;
                            if (pendingFuture == future) {
                                pendingFuture = null;
                            }
                        }
                    }
                });

        return future;
    }

    public static synchronized void onServersLoaded(Set<String> newServers) {
        servers = newServers;
        state = State.READY;
        if (pendingFuture != null && !pendingFuture.isDone()) {
            pendingFuture.complete(newServers);
            pendingFuture = null;
        }
    }

    public static CompletableFuture<Set<String>> waitForLoad(int timeoutSeconds, Runnable triggerLoad) {
        synchronized (BungeeCache.class) {
            if (state == State.READY) {
                return CompletableFuture.completedFuture(servers);
            }

            if (state == State.UNKNOWN) {
                state = State.LOADING;

                if (triggerLoad != null) {
                    try {
                        triggerLoad.run();
                    } catch (Exception e) {
                        state = State.UNKNOWN;
                        CompletableFuture<Set<String>> failed = new CompletableFuture<>();
                        failed.completeExceptionally(e);
                        return failed;
                    }
                }
            }

            if (pendingFuture != null && !pendingFuture.isDone()) {
                return pendingFuture;
            }

            CompletableFuture<Set<String>> future = new CompletableFuture<>();
            pendingFuture = future;

            CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS)
                    .execute(() -> {
                        synchronized (BungeeCache.class) {
                            if (!future.isDone()) {
                                future.complete(Set.of());
                                state = State.UNKNOWN;
                                if (pendingFuture == future) {
                                    pendingFuture = null;
                                }
                            }
                        }
                    });

            return future;
        }
    }

}