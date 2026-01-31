package com.tty.gui;

import com.tty.Log;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.gui.BaseInventory;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@GuiMeta(type = "offline_enderchest")
public class OfflineNBTEnderCheat extends BaseInventory {

    @Getter
    private NBTFileHandle data;
    @Getter
    private UUID target;

    public OfflineNBTEnderCheat(NBTFileHandle data, UUID target) {
        this.data = data;
        this.target = target;
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
    protected void beforeCreate() {

    }

    @Override
    public void clean() {
        this.data = null;
        this.target = null;
    }

    public void setItem(int index, ItemStack itemStack) {
        if (this.inventory == null) {
            Log.error("you must first open to create inventory");
            return;
        }
        this.inventory.setItem(index, itemStack);
    }

}
