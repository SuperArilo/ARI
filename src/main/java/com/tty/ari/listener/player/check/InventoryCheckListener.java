package com.tty.ari.listener.player.check;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.GuiKeyEnum;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.utils.ComponentUtils;
import com.tty.ari.Ari;
import com.tty.ari.dto.gui.PlayerInventoryCheckMenu;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.gui.PlayerInventoryEdit;
import com.tty.ari.states.gui.GuiManagerStateService;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InventoryCheckListener extends BaseGuiListener<PlayerInventoryEdit> {

    public InventoryCheckListener(@NotNull AbstractJavaPlugin plugin, @NotNull GuiKeyEnum guiType) {
        super(plugin, guiType);
    }

    @Override
    protected @NotNull FunctionHandler<PlayerInventoryEdit> registry() {
        FunctionHandler<PlayerInventoryEdit> handler = new FunctionHandler<>();
        handler.add(FunctionType.PLAYER_HELMET, ((event, inventoryEdit, player) -> this.changeEquipment(event, inventoryEdit)));
        handler.add(FunctionType.PLAYER_CHESTPLATE, ((event, inventoryEdit, player) -> this.changeEquipment(event, inventoryEdit)));
        handler.add(FunctionType.PLAYER_LEGGINGS, ((event, inventoryEdit, player) -> this.changeEquipment(event, inventoryEdit)));
        handler.add(FunctionType.PLAYER_BOOTS, ((event, inventoryEdit, player) -> this.changeEquipment(event, inventoryEdit)));
        handler.add(FunctionType.PLAYER_OFF_HAND, ((event, inventoryEdit, player) -> this.changeEquipment(event, inventoryEdit)));
        return handler;
    }

    @Override
    protected void whenClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
        PlayerInventoryCheckMenu menuConfig = (PlayerInventoryCheckMenu) holder.getBaseMenu();
        int slot = event.getSlot();
        event.setCancelled(true);

        List<Integer> binding = holder.getCombineInventory();
        int playerSlot = binding.indexOf(slot);
        if (playerSlot == -1) return;

        ItemStack cursor = event.getCursor().clone();
        ItemStack slotItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : null;

        ItemStack newCursor = cursor.clone();
        ItemStack newSlotItem = slotItem != null ? slotItem.clone() : null;

        if (cursor.isEmpty() && slotItem != null && !slotItem.isEmpty()) {
            newCursor = slotItem.clone();
            newSlotItem = null;
        } else if (!cursor.isEmpty() && (slotItem == null || slotItem.isEmpty())) {
            newCursor = null;
            newSlotItem = cursor.clone();
        } else if (!cursor.isEmpty() && slotItem != null && !slotItem.isEmpty()) {
            if (cursor.isSimilar(slotItem) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                int move = Math.min(cursor.getAmount(), space);
                newSlotItem.setAmount(slotItem.getAmount() + move);
                newCursor.setAmount(cursor.getAmount() - move);
                if (newCursor.getAmount() <= 0) {
                    newCursor = null;
                }
            } else {
                newCursor = slotItem.clone();
                newSlotItem = cursor.clone();
            }
        }

        if (holder.getMonitoree() instanceof Player monitored && monitored.isOnline()) {
            monitored.getInventory().setItem(playerSlot, newSlotItem);
        }

        for (GuiState guiState : service.getAllStates()) {
            if (!(guiState instanceof OnCheckPlayerGuiState state)) continue;
            if (!state.getMonitoree().equals(holder.getMonitoree())) continue;
            if (menuConfig.getShortcutBar().contains(slot) && menuConfig.getPlayerInventory().contains(slot)) continue;
            state.getMenu().getInventory().setItem(slot, newSlotItem != null ? newSlotItem.clone() : null);
        }

        event.getView().setCursor(newCursor);
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, PlayerInventoryEdit holder) {
        List<Integer> bindingSlots = holder.getCombineInventory();
        Set<Integer> draggedSlots = event.getInventorySlots();

        if (!new HashSet<>(bindingSlots).containsAll(draggedSlots)) {
            event.setCancelled(true);
            return;
        }

        InventoryView view = event.getView();
        Ari.instance.getScheduler().run(Ari.instance, i -> {
            if (view.getTopInventory() != holder.getInventory()) return;

            GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
            PlayerInventoryCheckMenu menuConfig = (PlayerInventoryCheckMenu) holder.getBaseMenu();

            for (int guiSlot : draggedSlots) {
                int playerSlot = bindingSlots.indexOf(guiSlot);
                if (playerSlot == -1) continue;

                ItemStack guiItem = view.getItem(guiSlot);
                ItemStack newItem = guiItem != null ? guiItem.clone() : null;

                if (holder.getMonitoree() instanceof Player monitored && monitored.isOnline()) {
                    monitored.getInventory().setItem(playerSlot, newItem);
                }

                for (GuiState guiState : service.getAllStates()) {
                    if (!(guiState instanceof OnCheckPlayerGuiState state)) continue;
                    if (!state.getMonitoree().equals(holder.getMonitoree())) continue;
                    if (menuConfig.getShortcutBar().contains(guiSlot) && menuConfig.getPlayerInventory().contains(guiSlot)) continue;
                    state.getMenu().getInventory().setItem(guiSlot, newItem != null ? newItem.clone() : null);
                }
            }
        });
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        event.setCancelled(true);
    }

    private void changeEquipment(InventoryClickEvent event, PlayerInventoryEdit inventoryEdit) {
        ItemStack clickItem = event.getCurrentItem();
        if (clickItem == null) return;
        ItemStack cursor = event.getCursor();

        FunctionType type = this.ItemNBT_TypeCheck(Ari.instance.getNbtManager().getNbt(NbtGuiValue.GUI_FUNCTION_ICON, clickItem, PersistentDataType.STRING));
        if (type == null) return;

        Map<String, FunctionItems> functionItems = inventoryEdit.getBaseMenu().getFunctionItems();
        OfflinePlayer monitoree = inventoryEdit.getMonitoree();

        for (FunctionItems value : functionItems.values()) {
            if (!value.getType().equals(type)) continue;

            boolean isPlaceholderItem = clickItem.getType().name().equals(value.getMaterial());
            boolean isClickItemHasNBT = Ari.instance.getNbtManager().hasNbt(NbtGuiValue.GUI_FUNCTION_ICON, clickItem, PersistentDataType.STRING);

            ItemStack finalGuiItem = null;
            ItemStack finalPlayerItem = null;
            ItemStack finalCursor = null;

            if (cursor.isEmpty()) {
                if (isClickItemHasNBT && !isPlaceholderItem) {
                    finalGuiItem = this.createFunctionItem(value);
                    ItemStack clone = clickItem.clone();
                    Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, clone);
                    finalCursor = clone;
                }
                if (finalCursor == null) return;
            } else {
                FunctionType equipment = this.isEquipment(cursor, value);
                if (equipment == null || !equipment.equals(value.getType())) return;

                ItemStack cloneCursor = cursor.clone();
                Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, cloneCursor, PersistentDataType.STRING, value.getType().getName());

                if (isClickItemHasNBT && !isPlaceholderItem) {
                    ItemStack oldItemClone = clickItem.clone();
                    Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, oldItemClone);
                    finalCursor = oldItemClone;
                }

                finalGuiItem = cloneCursor;
                finalPlayerItem = cursor.clone();
            }

            if (monitoree instanceof Player player && player.isOnline()) {
                this.setEquipmentToPlayer(player, value.getType(), finalPlayerItem);
            }

            GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
            for (GuiState guiState : service.getAllStates()) {
                if (!(guiState instanceof OnCheckPlayerGuiState state)) continue;
                if (!state.getMonitoree().equals(monitoree)) continue;
                state.getMenu().getInventory().setItem(event.getSlot(), finalGuiItem.clone());
            }
            event.getView().setCursor(finalCursor);
            return;
        }
    }

    private ItemStack createFunctionItem(FunctionItems item) {
        ItemStack itemStack = ItemStack.of(Material.valueOf(item.getMaterial()));
        Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, item.getType().getName());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(ComponentUtils.text(item.getName()));
        List<TextComponent> collect = item.getLore().stream().map(ComponentUtils::text).toList();
        itemMeta.lore(collect);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private FunctionType isEquipment(@Nullable ItemStack item, FunctionItems functionItems) {
        if (item == null || item.getType().isAir() || functionItems.getType().equals(FunctionType.PLAYER_OFF_HAND)) return FunctionType.PLAYER_OFF_HAND;
        EquipmentSlot slot = item.getType().getEquipmentSlot();
        return switch (slot) {
            case HEAD -> FunctionType.PLAYER_HELMET;
            case CHEST -> FunctionType.PLAYER_CHESTPLATE;
            case LEGS -> FunctionType.PLAYER_LEGGINGS;
            case FEET -> FunctionType.PLAYER_BOOTS;
            default -> null;
        };
    }

    private void setEquipmentToPlayer(Player player, FunctionType type, @Nullable ItemStack stack) {
        PlayerInventory inventory = player.getInventory();
        switch (type) {
            case PLAYER_HELMET -> inventory.setHelmet(stack);
            case PLAYER_CHESTPLATE -> inventory.setChestplate(stack);
            case PLAYER_LEGGINGS -> inventory.setLeggings(stack);
            case PLAYER_BOOTS -> inventory.setBoots(stack);
            case PLAYER_OFF_HAND -> inventory.setItemInOffHand(stack);
        }
    }

}
