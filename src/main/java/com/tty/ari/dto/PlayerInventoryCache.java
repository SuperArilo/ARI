package com.tty.ari.dto;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;


public class PlayerInventoryCache {

    @Nullable
    @Getter
    private ItemStack off_hand;
    @Nullable
    @Getter
    private ItemStack helmet;
    @Nullable
    @Getter
    private ItemStack chestplate;
    @Nullable
    @Getter
    private ItemStack leggings;
    @Nullable
    @Getter
    private ItemStack boots;
    @Setter
    @Getter
    private ItemStack[] items = new ItemStack[36];

    public PlayerInventoryCache setOffHand(ItemStack itemStack) {
        this.off_hand = itemStack;
        return this;
    }

    public PlayerInventoryCache setHelmet(ItemStack itemStack) {
        this.helmet = itemStack;
        return this;
    }

    public PlayerInventoryCache setChestplate(ItemStack itemStack) {
        this.chestplate = itemStack;
        return this;
    }

    public PlayerInventoryCache setLeggings(ItemStack itemStack) {
        this.leggings = itemStack;
        return this;
    }

    public PlayerInventoryCache setBoots(ItemStack itemStack) {
        this.boots = itemStack;
        return this;
    }

    public PlayerInventoryCache addItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.length) throw new IllegalArgumentException("Slot out of range: " + slot);
        items[slot] = stack;
        return this;
    }

}
