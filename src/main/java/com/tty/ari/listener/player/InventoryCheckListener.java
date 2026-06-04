package com.tty.ari.listener.player;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.GuiKeyEnum;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.utils.ComponentUtils;
import com.tty.ari.dto.gui.PlayerInventoryCheckMenu;
import com.tty.ari.gui.PlayerInventoryEdit;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.tty.ari.gui.PlayerInventoryEdit.MAX_PLAYER_INVENTORY_INDEX;

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
    protected void whenClick(InventoryClickEvent event, PlayerInventoryEdit holder) {

        PlayerInventoryCheckMenu menu = (PlayerInventoryCheckMenu) holder.getBaseMenu();
        int slot = event.getSlot();
        boolean isGuiRound = menu.getShortcutBar().contains(slot) || menu.getPlayerInventory().contains(slot);
        ItemStack clickItem = event.getCurrentItem();
        event.setCancelled(true);
        if (!isGuiRound) {
            if (clickItem == null) return;

            ItemMeta itemMeta = clickItem.getItemMeta();
            FunctionType type = this.ItemNBT_TypeCheck(itemMeta.getPersistentDataContainer().get(this.getFunctionIconNamespacedKey(), PersistentDataType.STRING));
            if (type == null) return;

            Map<String, FunctionItems> functionItems = holder.getBaseMenu().getFunctionItems();
            functionItems.values().stream().filter(i -> i.getType().equals(type)).findFirst().ifPresent(items -> this.changeEquipment(event, items));

        } else {

            ItemStack cursor = event.getCursor();
            ItemStack slotItem = event.getCurrentItem();
            InventoryView view = event.getView();
            List<Integer> binding = holder.getCombineInventory();

            if (!(holder.getOfflinePlayer() instanceof Player player)) return;

            int playerSlot = binding.indexOf(event.getSlot());
            if (playerSlot == -1) return;

            if (cursor.isEmpty() && slotItem != null && !slotItem.isEmpty()) {
                view.setCursor(slotItem.clone());
                view.setItem(event.getSlot(), null);
                player.getInventory().setItem(playerSlot, null);

            } else if (!cursor.isEmpty() && (slotItem == null || slotItem.isEmpty())) {
                player.getInventory().setItem(playerSlot, cursor.clone());
                view.setItem(event.getSlot(), cursor.clone());
                view.setCursor(null);

            } else if (!cursor.isEmpty() && slotItem != null && !slotItem.isEmpty()) {
                if (cursor.isSimilar(slotItem) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                    int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                    int move = Math.min(cursor.getAmount(), space);
                    slotItem.setAmount(slotItem.getAmount() + move);
                    cursor.setAmount(cursor.getAmount() - move);
                    view.setItem(event.getSlot(), slotItem.clone());
                    player.getInventory().setItem(playerSlot, slotItem.clone());
                    if (cursor.getAmount() <= 0) {
                        view.setCursor(null);
                    } else {
                        view.setCursor(cursor.clone());
                    }
                } else {
                    view.setCursor(slotItem.clone());
                    view.setItem(event.getSlot(), cursor.clone());
                    player.getInventory().setItem(playerSlot, cursor.clone());
                }
            }
        }
    }

    @Override
    protected void whenDoubleClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        event.setCancelled(true);

        ItemStack cursor = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        InventoryView view = event.getView();
        List<Integer> combineSlots = holder.getCombineInventory();

        if (!(holder.getOfflinePlayer() instanceof Player player)) return;

        ItemStack targetType;
        if (!cursor.isEmpty()) {
            targetType = cursor.clone();
            targetType.setAmount(1);
        } else if (clickedItem != null && !clickedItem.isEmpty()) {
            targetType = clickedItem.clone();
            targetType.setAmount(1);
        } else return;

        int maxStack = targetType.getMaxStackSize();
        int cursorAmount = !cursor.isEmpty() ? cursor.getAmount() : 0;
        ItemStack newCursor = targetType.clone();
        newCursor.setAmount(cursorAmount);

        Map<Integer, ItemStack> updates = new HashMap<>();

        for (int guiSlot : combineSlots) {
            ItemStack item = view.getItem(guiSlot);
            if (item == null || item.isEmpty() || !item.isSimilar(targetType)) continue;

            int available = maxStack - newCursor.getAmount();
            if (available <= 0) break;

            int take = Math.min(item.getAmount(), available);
            newCursor.setAmount(newCursor.getAmount() + take);

            if (take == item.getAmount()) {
                updates.put(guiSlot, null);
            } else {
                ItemStack remaining = item.clone();
                remaining.setAmount(item.getAmount() - take);
                updates.put(guiSlot, remaining);
            }
        }

        if (newCursor.getAmount() == cursorAmount && cursor.isEmpty()) return;

        for (Map.Entry<Integer, ItemStack> entry : updates.entrySet()) {
            int guiSlot = entry.getKey();
            ItemStack newContent = entry.getValue();
            view.setItem(guiSlot, newContent != null ? newContent.clone() : null);

            int playerSlot = combineSlots.indexOf(guiSlot);
            if (playerSlot != -1) {
                player.getInventory().setItem(playerSlot, newContent != null ? newContent.clone() : null);
            }
        }

        view.setCursor(newCursor.getAmount() > 0 ? newCursor : null);
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, PlayerInventoryEdit holder) {
        List<Integer> bindingSlots = holder.getCombineInventory(); // GUI槽位编号列表，索引即玩家背包槽位
        Set<Integer> draggedSlots = event.getInventorySlots();

        if (!new HashSet<>(bindingSlots).containsAll(draggedSlots)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        InventoryView view = event.getView();
        Map<Integer, ItemStack> newItems = event.getNewItems();

        for (Map.Entry<Integer, ItemStack> entry : newItems.entrySet()) {
            int slot = entry.getKey();
            ItemStack newItem = entry.getValue();
            view.setItem(slot, newItem != null ? newItem.clone() : null);
            int playerSlot = bindingSlots.indexOf(slot);
            if (playerSlot != -1 && holder.getOfflinePlayer() instanceof Player player) {
                player.getInventory().setItem(playerSlot, newItem != null ? newItem.clone() : null);
            }
        }
        view.setCursor(null);
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, PlayerInventoryEdit holder) {

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            event.setCancelled(true);
            return;
        }

        PlayerInventoryCheckMenu menu = (PlayerInventoryCheckMenu) holder.getBaseMenu();

        List<Integer> slotMap = new ArrayList<>(MAX_PLAYER_INVENTORY_INDEX);
        slotMap.addAll(menu.getPlayerInventory());
        slotMap.addAll(menu.getShortcutBar());


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
                return;
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
                return;
            }
        }
        if (remaining != originalAmount) {
            clickedItem.setAmount(remaining);
            event.setCurrentItem(clickedItem);
        }
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
