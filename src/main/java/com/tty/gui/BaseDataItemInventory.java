package com.tty.gui;

import com.tty.Ari;
import com.tty.entity.menu.BaseDataMenu;
import com.tty.lib.Log;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class BaseDataItemInventory<T> extends BaseInventory {

    protected int pageNum = 1;
    protected final int pageSize;
    public final BaseDataMenu baseDataInstance;
    protected List<T> data;
    private final AtomicReference<CompletableFuture<List<T>>> currentRequest = new AtomicReference<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public BaseDataItemInventory(BaseDataMenu baseDataInstance, Player player) {
        super(baseDataInstance, player);
        this.baseDataInstance = baseDataInstance;
        this.pageSize = baseDataInstance.getDataItems().getSlot().size();
        this.requestAndAccept(list -> {
            this.data = list;
            this.renderDataItem();
        });
    }

    /**
     * 上一页
     */
    public void prev() {
        if (this.closed.get()) return;
        this.pageNum--;
        if(this.pageNum <= 0) {
            this.player.sendMessage(Ari.instance.dataService.getValue("base.page-change.none-prev"));
            this.pageNum = 1;
            return;
        }
        this.requestAndAccept(list -> {
            this.data = list;
            this.renderDataItem();
        });
    }

    /**
     * 下一页
     */
    public void next() {
        if (this.closed.get()) return;
        this.pageNum++;
        this.requestAndAccept(list -> {
            if (list.isEmpty()) {
                this.player.sendMessage(Ari.instance.dataService.getValue("base.page-change.none-next"));
                this.pageNum--;
            } else {
                this.data = list;
                this.renderDataItem();
            }
        });
    }

    /**
     * 请求数据的方法
     * @return 返回数据 CompletableFuture
     */
    protected abstract CompletableFuture<List<T>> requestData();
    protected abstract Map<Integer, ItemStack> getRenderItem();

    private void requestAndAccept(Consumer<List<T>> onSuccess) {
        try {
            CompletableFuture<List<T>> future = this.requestData();
            if (future == null) {
                onSuccess.accept(List.of());
                return;
            }
            this.currentRequest.set(future);
            future.thenAccept(list -> {
                // ignore if closed or this is not the latest request
                if (this.closed.get()) return;
                CompletableFuture<List<T>> cur = this.currentRequest.get();
                if (cur != future) return;
                try {
                    onSuccess.accept(list);
                } catch (Throwable t) {
                    Log.error(t, "%s: render data accept error!", this.holder != null ? this.holder.type().name() : "UNKNOWN");
                }
            }).exceptionally(ex -> {
                Log.error(ex, "%s: request data error!", this.holder != null ? this.holder.type().name() : "UNKNOWN");
                return null;
            });
        } catch (Throwable ex) {
            Log.error(ex, "%s: start request error!", this.holder != null ? this.holder.type().name() : "UNKNOWN");
        }
    }

    private void renderDataItem() {
        long l = System.currentTimeMillis();
        Map<Integer, ItemStack> renderItem = this.getRenderItem();
        if (renderItem == null || renderItem.isEmpty()) return;

        for (Integer index : this.baseDataInstance.getDataItems().getSlot()) {
            this.clearItem(index);
            if(renderItem.size() != index + 1) {
                ItemStack it = renderItem.get(index);
                if (it != null) this.setItem(index, it);
            }
        }
        Log.debug("%s: render data items: %sms", this.holder != null ? this.holder.type().name() : "UNKNOWN", (System.currentTimeMillis() - l));
    }

    @Override
    protected void onCleanup() {
        if (!this.closed.compareAndSet(false, true)) return;
        CompletableFuture<List<T>> f = this.currentRequest.getAndSet(null);
        if (f != null) {
            try {
                f.cancel(true);
            } catch (Throwable ignored) {}
        }
        this.data = null;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
