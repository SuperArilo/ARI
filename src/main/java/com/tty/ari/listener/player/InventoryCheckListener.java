package com.tty.ari.listener.player;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.GuiKeyEnum;
import com.tty.api.listener.BaseGuiListener;
import com.tty.ari.gui.PlayerInventoryEdit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
        return Arrays.stream(PlayerInventoryEdit.PLAYER_INVENTORY_SLOT_MAP).anyMatch(i -> i == event.getSlot());
    }

    @Override
    protected boolean whenDoubleClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        return false;
    }

    @Override
    protected boolean whenDrag(InventoryDragEvent event, PlayerInventoryEdit holder) {
        Set<Integer> slots = event.getInventorySlots();
        Set<Integer> slotSet = new HashSet<>();
        for (int i : PlayerInventoryEdit.PLAYER_INVENTORY_SLOT_MAP) {
            slotSet.add(i);
        }
        return !slots.isEmpty() && slotSet.containsAll(slots);
    }

    @Override
    protected boolean whenShiftClick(InventoryClickEvent event, PlayerInventoryEdit holder) {

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return false;

        int[] slotMap = PlayerInventoryEdit.PLAYER_INVENTORY_SLOT_MAP;
        Arrays.sort(slotMap);
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

}
