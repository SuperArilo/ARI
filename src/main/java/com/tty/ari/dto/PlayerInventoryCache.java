package com.tty.ari.dto;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class PlayerInventoryCache {

    @Nullable
    @Getter
    private ItemStack off_hand;

    @Setter
    @Nullable
    @Getter
    private ItemStack helmet;

    @Setter
    @Nullable
    @Getter
    private ItemStack chestplate;

    @Setter
    @Nullable
    @Getter
    private ItemStack leggings;

    @Setter
    @Nullable
    @Getter
    private ItemStack boots;

    /**
     * 玩家背包映射至箱子 gui 的索引布局 18 开始 53 结束
     */
    @Getter
    private final Map<Integer, ItemStack> items = new HashMap<>();

    public void setOffHand(ItemStack itemStack) {
        this.off_hand = itemStack;
    }

    public void addItem(int slot, ItemStack stack) {
        this.items.put(slot,stack);
    }

}
