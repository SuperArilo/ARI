package com.tty.gui;

import com.tty.Ari;
import com.tty.dto.CustomInventoryHolder;
import com.tty.entity.menu.BaseMenu;
import com.tty.entity.menu.FunctionItems;
import com.tty.entity.menu.Mask;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.tool.ComponentUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public abstract class BaseInventory {

    public BaseMenu baseInstance;
    protected final Player player;
    protected Inventory inventory;
    public CustomInventoryHolder holder;

    private final NamespacedKey renderType = new NamespacedKey(Ari.instance, "type");

    public BaseInventory(BaseMenu instance, Player player) {
        this.baseInstance = instance;
        this.player = player;
    }

    public void open() {
        this.holder = this.createHolder();
        this.inventory = Bukkit.createInventory(this.holder, this.baseInstance.getRow() * 9, ComponentUtils.text(this.baseInstance.getTitle(), player));
        this.player.openInventory(this.inventory);
        this.renderMasks();
        this.renderFunctionItems();
    }

    protected abstract Mask renderCustomMasks();

    protected abstract Map<String, FunctionItems> renderCustomFunctionItems();

    private void renderMasks() {
        long l = System.currentTimeMillis();
        Mask mask = this.renderCustomMasks();
        if (mask == null) {
            mask = this.baseInstance.getMask();
        }
        List<TextComponent> collect = mask.getLore().stream().map(i -> ComponentUtils.text(i, this.player)).toList();
        for (Integer i : mask.getSlot()) {
            ItemStack itemStack = new ItemStack(Material.valueOf(mask.getMaterial().toUpperCase()));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentUtils.text(mask.getName(), this.player));
            itemMeta.getPersistentDataContainer().set(this.renderType, PersistentDataType.STRING, FunctionType.MASK_ICON.name());
            itemMeta.lore(collect);
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(i, itemStack);
        }
        Log.debug("%s: render masks: %sms", this.holder.type().name(), (System.currentTimeMillis() - l));
    }

    private void renderFunctionItems() {
        long l = System.currentTimeMillis();
        Map<String, FunctionItems> functionItems = this.renderCustomFunctionItems();
        if (functionItems == null || functionItems.isEmpty()) {
            functionItems = this.baseInstance.getFunctionItems();
        }
        functionItems.forEach((k, v) -> {
            ItemStack o = new ItemStack(Material.valueOf(v.getMaterial().toUpperCase()));
            ItemMeta mo = o.getItemMeta();
            mo.displayName(ComponentUtils.text(v.getName(), this.player));
            mo.lore(v.getLore().stream().map(i -> ComponentUtils.text(i, this.player)).toList());
            mo.getPersistentDataContainer().set(this.renderType, PersistentDataType.STRING, v.getType().name());
            o.setItemMeta(mo);
            for (Integer integer : v.getSlot()) {
                this.inventory.setItem(integer, o);
            }
        });
        Log.debug("%s: render function items: %sms", this.holder.type().name(), (System.currentTimeMillis() - l));
    }

    protected String replaceKey(String content, Map<String, String> map) {
        if (content == null || map == null || map.isEmpty()) {
            return content;
        }
        String result = content;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result = result.replace("<" + entry.getKey() + ">" , entry.getValue());
            }
        }
        return result;
    }

    protected abstract CustomInventoryHolder createHolder();

    public void cleanup() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
        this.inventory = null;
        this.holder = null;
        this.baseInstance = null;
        this.onCleanup();
    }

    protected void onCleanup() {

    }
}
