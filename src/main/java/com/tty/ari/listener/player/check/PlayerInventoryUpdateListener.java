package com.tty.ari.listener.player.check;

import com.tty.api.gui.BaseInventory;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.gui.PlayerInventoryEdit;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;


public class PlayerInventoryUpdateListener implements Listener {

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BaseInventory) return;
        this.syncPlayerInventoryToEdit(event.getWhoClicked());
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof HumanEntity entity)) return;
        if (entity.getInventory().getHolder() instanceof BaseInventory) return;
        this.syncPlayerInventoryToEdit(entity);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        Player player = event.getPlayer();
        ItemStack before;
        if (event.getItem() == null) return;
        before = event.getItem().clone();
        Ari.instance.getScheduler().runLater(Ari.instance, i -> {
            ItemStack current = player.getInventory().getItemInMainHand();
            if (!before.equals(current)) {
                this.syncPlayerInventoryToEdit(player);
            }
        }, 1);
    }

    private void syncPlayerInventoryToEdit(HumanEntity entity) {
        Ari.instance.getScheduler().runAtEntity(Ari.instance, entity, i -> {
            GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
            for (GuiState guiState : service.getAllStates()) {
                if (!(guiState.getMenu() instanceof PlayerInventoryEdit editInventory) || !(guiState instanceof OnCheckPlayerGuiState state)) continue;

                if (!state.getMonitoree().equals(entity) || !state.getOwner().equals(editInventory.getOfflinePlayer())) return;
                ItemStack[] playerContents = entity.getInventory().getContents();
                for (int slot = 0; slot < entity.getInventory().getSize(); slot++) {
                    editInventory.setItem(slot, playerContents[slot]);
                }
            }
        }, null);
    }

}
