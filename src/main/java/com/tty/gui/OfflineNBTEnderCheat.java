package com.tty.gui;

import com.tty.Ari;
import com.tty.lib.enum_type.GuiType;
import com.tty.lib.gui.BaseInventory;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;


public class OfflineNBTEnderCheat extends BaseInventory {

    @Getter
    private NBTFileHandle data;
    @Getter
    private UUID target;

    public OfflineNBTEnderCheat(Player player, NBTFileHandle data, UUID target) {
        super(Ari.instance, player, GuiType.OFFLINE_ENDERCHEST);
        this.data = data;
        this.target = target;
    }

    @Override
    protected Inventory create() {
        return Bukkit.createInventory(this, InventoryType.ENDER_CHEST);
    }

    @Override
    public void clean() {
        this.data = null;
        this.target = null;
    }

    public void setItem(int index, ItemStack itemStack) {
        this.inventory.setItem(index, itemStack);
    }

}
