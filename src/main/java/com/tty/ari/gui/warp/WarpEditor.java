package com.tty.ari.gui.warp;

import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.entity.ServerWarp;
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

@GuiMeta(type = "warp_edit")
public class WarpEditor extends BaseConfigInventory {

    @Getter
    private volatile ServerWarp currentEditWarp;

    public WarpEditor(ServerWarp serverWarp, Player player) {
        super(Ari.instance, player);
        this.currentEditWarp = serverWarp;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.WARP_EDIT_GUI.name()).saveToString(), BaseMenu.class);
    }

    @Override
    protected void beforeRenderMasksAsync(@Nullable Mask mask) {}


    @Override
    protected void beforeRenderFunctionItemsAsync(Map<String, FunctionItems> functionItems) {
        if(functionItems != null) {
            for (FunctionItems item : functionItems.values()) {
                switch (item.getType()) {
                    case ICON -> item.setMaterial(this.currentEditWarp.getShowMaterial());
                    case RENAME -> item.setName(this.currentEditWarp.getWarpName());
                    case LOCATION -> {
                        Location location = FormatUtils.parseLocation(this.currentEditWarp.getLocation());
                        Map<String, String> m = new HashMap<>();
                        m.put(IconKeyType.X.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case PERMISSION -> {
                        String permission = this.currentEditWarp.getPermission();
                        item.setName(permission == null ? "":permission);
                    }
                    case COST -> {
                        if (Ari.ECONOMY_SERVICE.isNull()) {
                            item.setName(Ari.instance.getConfigInstance().getValue("server.message.no-economy", FilePath.LANG));
                            item.setMaterial("barrier");
                        } else {
                            Double cost = this.currentEditWarp.getCost();
                            item.setName(cost == null ? "":cost.toString());
                        }
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.DATA_SERVICE.getValue(this.currentEditWarp.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
                }
            }
        }
    }

    @Override
    protected void whenRenderComplete(@NotNull Inventory inventory) {

    }

    @Override
    protected CompletableFuture<Boolean> onClose() {
        return CompletableFuture.supplyAsync(() -> {
            this.currentEditWarp = null;
            return true;
        }, this.getExecutorAsync());
    }

}
