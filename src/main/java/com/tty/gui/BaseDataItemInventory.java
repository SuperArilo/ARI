package com.tty.gui;

import com.tty.Ari;
import com.tty.entity.menu.BaseDataMenu;
import com.tty.enumType.GuiType;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class BaseDataItemInventory<T> extends BaseInventory {

    protected int pageNum = 1;
    protected final int pageSize;
    public final BaseDataMenu baseDataInstance;
    protected List<T> data;

    public BaseDataItemInventory(BaseDataMenu baseDataInstance, Player player, GuiType type) {
        super(baseDataInstance, player, type);
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
        CompletableFuture<List<T>> future = this.requestData();
        if (future == null) {
            onSuccess.accept(List.of());
            return;
        }
        future.thenAccept(onSuccess).exceptionally(ex -> {
            Log.error(ex, "%s: request data error!", this.type.name());
            return null;
        });
    }

    private void renderDataItem() {
        long l = System.currentTimeMillis();
        Map<Integer, ItemStack> renderItem = this.getRenderItem();
        if (renderItem == null || renderItem.isEmpty()) return;

        for (Integer index : this.baseDataInstance.getDataItems().getSlot()) {
            Lib.Scheduler.runAtEntity(
                    Ari.instance,
                    this.player,
                    i -> {
                        if (this.inventory == null) return;
                        this.inventory.clear(index);
                        this.inventory.setItem(index, renderItem.get(index));
                    },
                    null);
        }
        Log.debug("%s: submit render task time: %sms", this.type.name(), (System.currentTimeMillis() - l));
    }

    /**
     * 根据材质字符串来创建对于的 ItemStack
     * @param showMaterial 材质字符串
     * @return ItemStack 如果不存在则是 null
     */
    protected ItemStack createItemStack(@Nullable String showMaterial) {
        if (showMaterial == null) return null;
        ItemStack itemStack = null;
        try {
            itemStack = new ItemStack(Material.valueOf(showMaterial.toUpperCase()));
            return itemStack;
        } catch (Exception e) {
            Log.error(e, "create ItemStack error. material %s", showMaterial);
            return itemStack;
        }

    }

    /**
     * 将指定的 ItemStack 的 ItemMeta 设置为高亮模式
     * @param itemMeta ItemStack 的 ItemMeta
     */
    protected void setHighlight(@NotNull ItemMeta itemMeta) {
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    protected void onCleanup() {
        this.data = null;
    }
}
