package com.tty.ari.listener.player.check;

import com.tty.api.gui.BaseInventory;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.gui.PlayerInventoryEdit;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;


public class PlayerInventoryUpdateListener implements Listener {

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BaseInventory || !(event.getWhoClicked() instanceof Player player)) return;
        this.syncPlayerInventoryToEdit(player, inventoryEdit -> this.updateInventory(player, inventoryEdit));
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        this.syncPlayerInventoryToEdit(player, inventoryEdit -> this.updateInventory(player, inventoryEdit));
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        Player player = event.getPlayer();
        ItemStack before;
        if (event.getItem() == null) return;
        before = event.getItem().clone();
        Ari.instance.getScheduler().runLater(i -> {
            ItemStack current = player.getInventory().getItemInMainHand();
            if (!before.equals(current)) {
                this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
            }
        }, 1);
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> this.updateInventory(event.getPlayer(), inventoryEdit));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location eventTo = event.getTo();
        Player player = event.getPlayer();
        this.syncPlayerInventoryToEdit(player, inventoryEdit -> inventoryEdit.updateLocation(eventTo));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.syncPlayerInventoryToEdit(event.getPlayer(), inventoryEdit -> inventoryEdit.updateLocation(null));
    }

    private void syncPlayerInventoryToEdit(Player player, Consumer<PlayerInventoryEdit> consumer) {
        GuiManagerStateService service = Ari.instance.getStatusManager().get(GuiManagerStateService.class);
        Ari.instance.getScheduler().runLater(i -> {
            for (GuiState guiState : service.getAllStates()) {
                if (!(guiState.getMenu() instanceof PlayerInventoryEdit editInventory) || !(guiState instanceof OnCheckPlayerGuiState state)) continue;
                if (state.isUpdating()) continue;
                if (!state.getMonitoree().equals(player) || !state.getOwner().equals(editInventory.getOfflinePlayer())) continue;
                state.setUpdating(true);
                try {
                    consumer.accept(editInventory);
                } catch (Exception e) {
                    Ari.instance.getLog().error(e);
                } finally {
                    state.setUpdating(false);
                }
            }
        }, 5L);
    }

    private void updateInventory(Player player, PlayerInventoryEdit inventoryEdit) {
        ItemStack[] playerContents = player.getInventory().getContents();
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            inventoryEdit.setAbstractItem(slot, playerContents[slot]);
        }
        inventoryEdit.setExperience(player.getTotalExperience(), player.getExp(), player.getLevel());
    }

}
