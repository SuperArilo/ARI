package com.tty.ari.listener;

import com.tty.api.gui.BaseInventory;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class GuiCleanupListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseInventory baseInventory)) return;
        GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
        for (GuiState state : service.getStates(event.getPlayer())) {
            if(state.getMenu().equals(baseInventory)) {
                state.setOver(true);
            }
        }
    }
}
