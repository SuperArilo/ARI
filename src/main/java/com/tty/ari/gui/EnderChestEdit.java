package com.tty.ari.gui;

import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.gui.BaseInventory;
import com.tty.api.utils.ComponentUtils;
import com.tty.ari.Ari;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "offline_enderchest")
public class EnderChestEdit extends BaseInventory {

    @Getter
    private Player surveillant;

    @Getter
    private OfflinePlayer monitoree;

    public EnderChestEdit(Player surveillant, OfflinePlayer monitoree) {
        super(Ari.instance);
        this.surveillant = surveillant;
        this.monitoree = monitoree;
    }

    @Override
    protected int size() {
        return InventoryType.ENDER_CHEST.getDefaultSize();
    }

    @Override
    protected @NotNull Component title() {
        return InventoryType.ENDER_CHEST.defaultTitle();
    }

    @Override
    protected void afterCreatedInventory(@NotNull Inventory inventory) {
        this.getPlugin().getScheduler().runAsync(this.getPlugin(), i -> {
            NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(this.monitoree.getUniqueId().toString());
            if (data == null) {
                this.surveillant.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                Ari.instance.getLog().error("uuid is not exist.", this.monitoree.getName());
                return;
            }
            ReadWriteNBTCompoundList enderItems = data.getCompoundList("EnderItems");

            for (ReadWriteNBT enderItem : enderItems) {
                int slot = enderItem.getByte("Slot") & 0xFF;
                ItemStack itemStack = NBT.itemStackFromNBT(enderItem);
                this.getPlugin().getScheduler().run(this.getPlugin(), t -> inventory.setItem(slot, itemStack));
            }
        });
    }

    @Override
    protected CompletableFuture<Boolean> onClose() {
        if (this.monitoree.isOnline()) {
            this.surveillant = null;
            this.monitoree = null;
            return CompletableFuture.completedFuture(true);
        }
        return CompletableFuture.supplyAsync(() -> {
            NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(this.monitoree.getUniqueId().toString());
            if (data == null) {
                this.surveillant.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                this.getPlugin().getLog().error("uuid is not exist.", this.monitoree.getName());
                return true;
            }
            data.removeKey("EnderItems");
            ReadWriteNBTCompoundList enderItems = data.getCompoundList("EnderItems");
            for (int slot = 0; slot < this.getInventory().getSize(); slot++) {
                ItemStack item = this.getInventory().getItem(slot);
                if (item == null || item.getType().isAir()) continue;
                ReadWriteNBT itemNBT = NBT.itemStackToNBT(item);
                itemNBT.setByte("Slot", (byte) slot);
                enderItems.addCompound(itemNBT);
            }
            try {
                data.save();
                this.getPlugin().getLog().debug("ender chest nbt save player {} success.", this.monitoree.getName());
            } catch (IOException e) {
                this.getPlugin().getLog().error(e, "ender chest nbt save error. ");
            } finally {
                this.surveillant = null;
                this.monitoree = null;
            }
            return true;
        }, this.getExecutorAsync());
    }

}
