package com.tty.function;


import com.tty.lib.dto.PageResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class BaseManager<T> {

    /**
     * 是否异步
     */
    public boolean isAsync;

    public BaseManager(boolean isAsync) {
        this.isAsync = isAsync;
    }

    /**
     * 设置执行模式
     * @param async true: 异步模式, false: 同步模式
     */
    public void setExecutionMode(boolean async) {
        this.isAsync = async;
    }

    protected <R> CompletableFuture<R> executeTask(Supplier<R> task) {
        if (this.isAsync) {
            return CompletableFuture.supplyAsync(task);
        } else {
            try {
                return CompletableFuture.completedFuture(task.get());
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        }
    }

    public abstract CompletableFuture<PageResult<T>> getList(int pageNum, int pageSize);

    public abstract CompletableFuture<Boolean> createInstance(T instance);

    public abstract CompletableFuture<Boolean> deleteInstance(T instance);
    /**
     * 修改信息
     * @param instance 被修改的对象
     * @return 修改成功状态。true：成功，false：失败
     */
    public abstract CompletableFuture<Boolean> modify(T instance);

}
