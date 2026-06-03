package com.tty.ari.gui;

import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.gui.BaseInventory;
import com.tty.ari.Ari;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
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
        super(Ari.instance);
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
    protected void afterCreatedInventory(@NotNull Inventory inventory) {}

    @Override
    public void close() {
        this.data = null;
        this.target = null;
    }

    public void setItem(int index, ItemStack itemStack) {
        this.getInventory().setItem(index, itemStack);
    }

}
