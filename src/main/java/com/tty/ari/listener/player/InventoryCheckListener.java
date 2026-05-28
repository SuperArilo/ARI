package com.tty.ari.listener.player;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.GuiKeyEnum;
import com.tty.api.listener.BaseGuiListener;
import com.tty.ari.gui.PlayerInventoryEdit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

public class InventoryCheckListener extends BaseGuiListener<PlayerInventoryEdit> {

    public InventoryCheckListener(@NotNull AbstractJavaPlugin plugin, @NotNull GuiKeyEnum guiType) {
        super(plugin, guiType);
    }

    @Override
    protected @NotNull FunctionHandler<PlayerInventoryEdit> registry() {
        FunctionHandler<PlayerInventoryEdit> handler = new FunctionHandler<>();



        return handler;
    }

    @Override
    protected boolean cancelClick(InventoryClickEvent event, PlayerInventoryEdit holder) {
        return false;
    }

    @Override
    protected boolean cancelDrag(InventoryDragEvent event, PlayerInventoryEdit holder) {
        return false;
    }


    @Override
    public void passClick(InventoryClickEvent event) {

    }

}
