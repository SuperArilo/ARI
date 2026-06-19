package com.tty.ari.listener.player.check;

import com.tty.api.gui.BaseInventory;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.gui.PlayerInventoryEdit;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;


public class PlayerInventoryUpdateListener implements Listener {

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BaseInventory || !(event.getWhoClicked() instanceof Player player)) return;
        this.syncPlayerInventoryToEdit(player);
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        this.syncPlayerInventoryToEdit(player);
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

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Ari.instance.getScheduler().runLater(Ari.instance, i -> this.syncPlayerInventoryToEdit(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Ari.instance.getScheduler().runLater(Ari.instance, i -> this.syncPlayerInventoryToEdit(event.getEntity()), 1L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Ari.instance.getScheduler().runLater(Ari.instance, i -> this.syncPlayerInventoryToEdit(event.getPlayer()), 1L);
    }

    private void syncPlayerInventoryToEdit(Player player) {
        GuiManagerStateService service = Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class);
        Ari.instance.getScheduler().runAtEntity(Ari.instance, player, i -> {
            for (GuiState guiState : service.getAllStates()) {
                if (!(guiState.getMenu() instanceof PlayerInventoryEdit editInventory) || !(guiState instanceof OnCheckPlayerGuiState state)) continue;
                if (!state.getMonitoree().equals(player) || !state.getOwner().equals(editInventory.getOfflinePlayer())) continue;
                state.setUpdating(true);
                ItemStack[] playerContents = player.getInventory().getContents();
                try {
                    for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                        editInventory.setItem(slot, playerContents[slot]);
                    }
                } catch (Exception e) {
                    Ari.instance.getLog().error(e);
                } finally {
                    state.setUpdating(false);
                }
            }
        }, null);
    }

}
