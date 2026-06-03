package com.tty.ari.gui.home;

import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.enumType.FilePath;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "home_edit")
public class HomeEditor extends BaseConfigInventory {

    @Getter
    private volatile ServerHome currentEditHome;

    public HomeEditor(ServerHome serverHome, Player player) {
        super(Ari.instance, player);
        this.currentEditHome = serverHome;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.HOME_EDIT_GUI.name()).saveToString(), BaseMenu.class);
    }

    @Override
    protected @NotNull CompletableFuture<Mask> beforeRenderMasksAsync(@Nullable Mask mask) {
        return CompletableFuture.completedFuture(mask);
    }

    @Override
    protected @NotNull CompletableFuture<Map<String, FunctionItems>> beforeRenderFunctionItemsAsync(Map<String, FunctionItems> functionItems) {
        if (functionItems != null) {
            for (FunctionItems item : functionItems.values()) {
                switch (item.getType()) {
                    case ICON -> item.setMaterial(this.currentEditHome.getShowMaterial());
                    case RENAME -> item.setName(this.currentEditHome.getHomeName());
                    case LOCATION -> {
                        Location location = FormatUtils.parseLocation(this.currentEditHome.getLocation());
                        Map<String, String> m = new HashMap<>();
                        m.put(IconKeyType.X.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.DATA_SERVICE.getValue(this.currentEditHome.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
                }
            }
        }
        return CompletableFuture.completedFuture(functionItems);
    }

    @Override
    protected void whenRenderComplete(@NotNull Inventory inventory) {

    }

    @Override
    public void close() {
        this.currentEditHome = null;
    }

}
