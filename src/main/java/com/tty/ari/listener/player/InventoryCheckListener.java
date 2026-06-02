package com.tty.ari.listener.player;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.GuiKeyEnum;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.utils.ComponentUtils;
import com.tty.ari.gui.PlayerInventoryEdit;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InventoryCheckListener extends BaseGuiListener<PlayerInventoryEdit> {

    public InventoryCheckListener(@NotNull AbstractJavaPlugin plugin, @NotNull GuiKeyEnum guiType) {
        super(plugin, guiType);
    }

    @Override
    protected @NotNull FunctionHandler<PlayerInventoryEdit> registry() {
        FunctionHandler<PlayerInventoryEdit> handler = new FunctionHandler<>();
        return handler;
    }

    @Override
    protected boolean whenClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        boolean fallback =PlayerInventoryEdit.PLAYER_INVENTORY_SLOT_MAP.contains(event.getSlot());

        ItemStack clickItem = event.getCurrentItem();
        if (clickItem == null) return fallback;

        ItemMeta itemMeta = clickItem.getItemMeta();
        FunctionType type = this.ItemNBT_TypeCheck(itemMeta.getPersistentDataContainer().get(this.getFunctionIconNamespacedKey(), PersistentDataType.STRING));
        if (type == null) return fallback;

        Map<String, FunctionItems> functionItems = holder.getBaseMenu().getFunctionItems();

        FunctionItems items = functionItems.values().stream().filter(i -> i.getType().equals(type)).findFirst().orElse(null);

        if (items == null) return fallback;

        this.changeEquipment(event, items);

        return fallback;
    }

    @Override
    protected boolean whenDoubleClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        return true;
    }

    @Override
    protected boolean whenDrag(InventoryDragEvent event, PlayerInventoryEdit holder) {
        Set<Integer> slots = event.getInventorySlots();
        Set<Integer> slotSet = new HashSet<>(PlayerInventoryEdit.PLAYER_INVENTORY_SLOT_MAP);
        return !slots.isEmpty() && slotSet.containsAll(slots);
    }

    @Override
    protected boolean whenShiftClick(InventoryClickEvent event, PlayerInventoryEdit holder) {

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return false;

        List<Integer> slotMap = PlayerInventoryEdit.PLAYER_INVENTORY_SLOT_MAP;
        Inventory gui = event.getInventory();

        int originalAmount = clickedItem.getAmount();
        int remaining = originalAmount;

        for (int guiSlot : slotMap) {

            ItemStack target = gui.getItem(guiSlot);
            if (target == null || target.getType().isAir()) continue;
            if (!target.isSimilar(clickedItem)) continue;
            int max = target.getMaxStackSize();
            int current = target.getAmount();
            if (current >= max) continue;
            int canMerge = max - current;
            int mergeAmount = Math.min(canMerge, remaining);

            target.setAmount(current + mergeAmount);
            remaining -= mergeAmount;

            if (remaining <= 0) {
                event.setCurrentItem(null);
                return true;
            }
        }

        for (int guiSlot : slotMap) {

            ItemStack target = gui.getItem(guiSlot);
            if (target != null && !target.getType().isAir()) continue;
            ItemStack clone = clickedItem.clone();
            int placeAmount = Math.min(clone.getMaxStackSize(), remaining);
            clone.setAmount(placeAmount);
            gui.setItem(guiSlot, clone);
            remaining -= placeAmount;
            if (remaining <= 0) {
                event.setCurrentItem(null);
                return true;
            }
        }
        if (remaining != originalAmount) {
            clickedItem.setAmount(remaining);
            event.setCurrentItem(clickedItem);
            return false;
        }
        return false;
    }

    @Override
    public void passClick(InventoryClickEvent event) {

    }

    private void changeEquipment(InventoryClickEvent event, FunctionItems items) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (current == null) return;

        Inventory inventory = event.getInventory();

        boolean isPlaceholderItem = current.getType().name().equals(items.getMaterial());
        boolean isClickItemHasNBT = current.getItemMeta().getPersistentDataContainer().has(this.getFunctionIconNamespacedKey());

        // 光标为空 -> 尝试取出装备
        if (cursor.isEmpty()) {
            if (isClickItemHasNBT && !isPlaceholderItem) {
                // 槽位上是已放入的装备，将其移到光标，槽位恢复占位符
                inventory.setItem(event.getSlot(), this.createFunctionItem(items));
                ItemStack equipmentWithoutTag = this.removeFunctionTag(current.clone());
                event.getView().setCursor(equipmentWithoutTag);
            }
            return;
        }

        // 光标非空，检查装备类型是否匹配
        if (!this.isEquipment(cursor, items).equals(items.getType())) return;

        // 槽位已有装备 -> 交换；否则为占位符/普通物品 -> 放置
        if (isClickItemHasNBT && !isPlaceholderItem) {
            // 交换：光标物品放入槽位（添加功能标签），槽位物品移到光标（移除功能标签）
            ItemStack slotItemWithoutTag = this.removeFunctionTag(current.clone());
            event.getView().setCursor(slotItemWithoutTag);

            ItemStack cursorItemWithTag = this.addFunctionTag(cursor.clone(), items.getType().name());
            inventory.setItem(event.getSlot(), cursorItemWithTag);
        } else {
            // 放置：光标物品放入槽位并添加功能标签，清空光标
            ItemStack cursorItemWithTag = this.addFunctionTag(cursor.clone(), items.getType().name());
            inventory.setItem(event.getSlot(), cursorItemWithTag);
            event.getView().setCursor(null);
        }
    }

    /**
     * 为物品添加功能NBT标签
     */
    private ItemStack addFunctionTag(ItemStack item, String typeName) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(this.getFunctionIconNamespacedKey(), PersistentDataType.STRING, typeName);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 移除物品的功能NBT标签
     */
    private ItemStack removeFunctionTag(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(this.getFunctionIconNamespacedKey());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFunctionItem(FunctionItems item) {
        ItemStack itemStack = ItemStack.of(Material.valueOf(item.getMaterial()));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(ComponentUtils.text(item.getName()));
        List<TextComponent> collect = item.getLore().stream().map(ComponentUtils::text).toList();
        itemMeta.lore(collect);
        itemMeta.getPersistentDataContainer().set(this.getFunctionIconNamespacedKey(), PersistentDataType.STRING, item.getType().name());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @NotNull
    private FunctionType isEquipment(@Nullable ItemStack item, FunctionItems functionItems) {
        if (item == null || item.getType().isAir() || functionItems.getType().equals(FunctionType.PLAYER_OFF_HAND)) return FunctionType.PLAYER_OFF_HAND;
        EquipmentSlot slot = item.getType().getEquipmentSlot();
        return switch (slot) {
            case HEAD -> FunctionType.PLAYER_HELMET;
            case CHEST -> FunctionType.PLAYER_CHESTPLATE;
            case LEGS -> FunctionType.PLAYER_LEGGINGS;
            case FEET -> FunctionType.PLAYER_BOOTS;
            default -> FunctionType.PLAYER_OFF_HAND;
        };
    }

}
