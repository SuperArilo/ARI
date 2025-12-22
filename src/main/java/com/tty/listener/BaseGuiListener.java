package com.tty.listener;

import com.tty.enumType.GuiType;
import com.tty.gui.BaseInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;


public abstract class BaseGuiListener implements Listener {

    protected final GuiType guiType;

    protected BaseGuiListener(GuiType guiType) {
        this.guiType = guiType;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        BaseInventory clickedHolder = clickedInventory.getHolder() instanceof BaseInventory c ? c : null;
        BaseInventory topHolder = topInventory.getHolder() instanceof BaseInventory t ? t : null;

        if (clickedHolder == null) return;

        boolean isCustomGui = clickedHolder.type.equals(this.guiType);

        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR && isCustomGui) {
            event.setCancelled(true);
            return;
        }

        if (topHolder != null && isCustomGui) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (event.isShiftClick()) return;
            this.passClick(event);
            return;
        }

        if (topHolder != null && event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        Inventory topInventory = view.getTopInventory();

        if (!(topInventory.getHolder() instanceof BaseInventory holder &&
                holder.type.equals(this.guiType))) {
            return;
        }

        int topSize = topInventory.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                break;
            }
        }
    }

    /**
     * 当点击通过 GUI 检查时调用，由子类实现具体点击处理逻辑
     *
     * @param event InventoryClickEvent
     */
    public abstract void passClick(InventoryClickEvent event);
}
