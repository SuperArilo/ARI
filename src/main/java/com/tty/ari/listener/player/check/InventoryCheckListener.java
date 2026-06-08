package com.tty.ari.listener.player.check;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.GuiKeyEnum;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.utils.ComponentUtils;
import com.tty.ari.Ari;
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


            int playerSlot = binding.indexOf(event.getSlot());
            if (playerSlot == -1) return;

            if (cursor.isEmpty() && slotItem != null && !slotItem.isEmpty()) {
                view.setCursor(slotItem.clone());
                view.setItem(event.getSlot(), null);
                if (holder.getMonitoree() instanceof Player player) {
                    player.getInventory().setItem(playerSlot, null);
                }


            } else if (!cursor.isEmpty() && (slotItem == null || slotItem.isEmpty())) {
                if (holder.getMonitoree() instanceof Player player) {
                    player.getInventory().setItem(playerSlot, cursor.clone());
                }

                view.setItem(event.getSlot(), cursor.clone());
                view.setCursor(null);

            } else if (!cursor.isEmpty() && slotItem != null && !slotItem.isEmpty()) {
                if (cursor.isSimilar(slotItem) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                    int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                    int move = Math.min(cursor.getAmount(), space);
                    slotItem.setAmount(slotItem.getAmount() + move);
                    cursor.setAmount(cursor.getAmount() - move);
                    view.setItem(event.getSlot(), slotItem.clone());
                    if (holder.getMonitoree() instanceof Player player) {
                        player.getInventory().setItem(playerSlot, slotItem.clone());
                    }
                    if (cursor.getAmount() <= 0) {
                        view.setCursor(null);
                    } else {
                        view.setCursor(cursor.clone());
                    }
                } else {
                    view.setCursor(slotItem.clone());
                    view.setItem(event.getSlot(), cursor.clone());
                    if (holder.getMonitoree() instanceof Player player) {
                        player.getInventory().setItem(playerSlot, cursor.clone());
                    }
                }
            }
        }
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, PlayerInventoryEdit holder) {
        List<Integer> bindingSlots = holder.getCombineInventory();
        Set<Integer> draggedSlots = event.getInventorySlots();

        if (!new HashSet<>(bindingSlots).containsAll(draggedSlots)) {
            event.setCancelled(true);
            return;
        }
        if (!(holder.getMonitoree() instanceof Player player)) return;

        InventoryView view = event.getView();
        Ari.instance.getScheduler().run(Ari.instance, i -> {
            if (!player.isOnline() || view.getTopInventory() != holder.getInventory()) return;
            for (int guiSlot : draggedSlots) {
                int playerSlot = bindingSlots.indexOf(guiSlot);
                if (playerSlot != -1) {
                    ItemStack guiItem = view.getItem(guiSlot);
                    player.getInventory().setItem(playerSlot, guiItem != null ? guiItem.clone() : null);
                }
            }
        });
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        event.setCancelled(true);
    }

    @Override
    public void passClick(InventoryClickEvent event) {}

    private void changeEquipment(InventoryClickEvent event, FunctionItems items) {

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (current == null) return;

        Inventory inventory = event.getInventory();

        boolean isPlaceholderItem = current.getType().name().equals(items.getMaterial());
        boolean isClickItemHasNBT = current.getItemMeta().getPersistentDataContainer().has(this.getFunctionIconNamespacedKey());

        if (cursor.isEmpty()) {
            if (isClickItemHasNBT && !isPlaceholderItem) {
                inventory.setItem(event.getSlot(), this.createFunctionItem(items));
                ItemStack equipmentWithoutTag = this.removeFunctionTag(current.clone());
                event.getView().setCursor(equipmentWithoutTag);
            }
            return;
        }

        if (!this.isEquipment(cursor, items).equals(items.getType())) return;

        if (isClickItemHasNBT && !isPlaceholderItem) {
            ItemStack slotItemWithoutTag = this.removeFunctionTag(current.clone());
            event.getView().setCursor(slotItemWithoutTag);

            ItemStack cursorItemWithTag = this.addFunctionTag(cursor.clone(), items.getType().name());
            inventory.setItem(event.getSlot(), cursorItemWithTag);
        } else {
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
