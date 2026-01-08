package com.tty.listener;

import com.tty.lib.gui.BaseInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;


public class GuiCleanupListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.clean(event.getInventory());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryView view = player.getOpenInventory();
        this.clean(view.getTopInventory());
    }

    private void clean(Inventory inv) {
        if (inv.getHolder() instanceof BaseInventory baseInventory) {
            baseInventory.cleanup();
        }
    }
}
