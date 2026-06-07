package com.tty.ari.listener.player.check;

import com.tty.api.gui.BaseInventory;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.gui.PlayerInventoryEdit;
import com.tty.ari.states.GuiManagerStateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerInventoryUpdateListener implements Listener {

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {

        if (event.getInventory().getHolder() instanceof BaseInventory) return;

        GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
        Ari.instance.getScheduler().run(Ari.instance, i -> {
            for (GuiState<PlayerInventoryEdit> state : service.getAllStates()) {
                PlayerInventoryEdit inventory = state.getMenu();

                ItemStack currentItem = event.getCurrentItem();
                int slot = event.getSlot();

                List<Integer> combineSlots = inventory.getCombineInventory();

                inventory.getInventory().setItem(combineSlots.get(slot), currentItem);
            }
        });

    }

    @EventHandler
    public void onPlayerPickup(InventoryPickupItemEvent event) {

    }

    @EventHandler
    public void onPlayerDrag(InventoryDragEvent event) {

    }

}
