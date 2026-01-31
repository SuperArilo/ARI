package com.tty.listener;

import com.tty.Ari;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.gui.OfflineNBTEnderCheat;
import com.tty.api.Log;
import com.tty.enumType.GuiType;
import com.tty.api.gui.BaseInventory;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

import static com.tty.commands.sub.EnderChestToPlayer.OFFLINE_ON_EDIT_ENDER_CHEST_LIST;


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

    private void clean(Inventory inventory) {
        if (inventory.getHolder() instanceof BaseInventory baseInventory) {
            GuiMeta annotation = baseInventory.getClass().getAnnotation(GuiMeta.class);
            if (annotation == null) return;
            if (annotation.type().equals(GuiType.OFFLINE_ENDERCHEST) && baseInventory instanceof OfflineNBTEnderCheat cheat) {
                NBTFileHandle data = cheat.getData();
                data.removeKey("EnderItems");
                ReadWriteNBTCompoundList enderItems = data.getCompoundList("EnderItems");
                Inventory inv = cheat.getInventory();
                for (int slot = 0; slot < inv.getSize(); slot++) {
                    ItemStack item = inv.getItem(slot);
                    if (item == null || item.getType().isAir()) continue;
                    ReadWriteNBT itemNBT = NBT.itemStackToNBT(item);
                    itemNBT.setByte("Slot", (byte) slot);
                    enderItems.addCompound(itemNBT);
                }
                Ari.SCHEDULER.runAsync(Ari.instance, i -> {
                    try {
                        data.save();
                        Log.debug("ender chest nbt save player {} success.", cheat.getTarget().toString());
                        OFFLINE_ON_EDIT_ENDER_CHEST_LIST.remove(cheat.getTarget());
                        Ari.SCHEDULER.run(Ari.instance, t -> baseInventory.cleanup());
                    } catch (IOException e) {
                        Log.error(e, "ender chest nbt save error. ");
                    }
                });
            } else {
                baseInventory.cleanup();
            }
        }
    }
}
