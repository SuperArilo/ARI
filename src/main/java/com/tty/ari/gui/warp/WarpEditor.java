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
import com.tty.ari.enumType.LangFile;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@GuiMeta(type = "warp_edit")
public class WarpEditor extends BaseConfigInventory {

    @Getter
    private volatile ServerWarp warp;

    public WarpEditor(Player player, ServerWarp serverWarp) {
        super(Ari.instance, player);
        this.warp = serverWarp;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return Ari.instance.getConfigInstance().yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.WARP_EDIT_GUI).saveToString(), BaseMenu.class);
    }

    @Override
    protected void beforeRenderMasksAsync(@Nullable Mask mask) {}


    @Override
    protected void beforeRenderFunctionItemsAsync(Map<String, FunctionItems> functionItems) {
        if(functionItems != null) {
            for (FunctionItems item : functionItems.values()) {
                switch (item.getType()) {
                    case ICON -> item.setMaterial(this.warp.getShowMaterial());
                    case RENAME -> item.setName(this.warp.getWarpName());
                    case LOCATION -> {
                        Location location = FormatUtils.parseLocation(this.warp.getLocation());
                        Map<IconKeyType, String> m = new HashMap<>();
                        m.put(IconKeyType.X, FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y, FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z, FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case PERMISSION -> {
                        String permission = this.warp.getPermission();
                        item.setName(permission == null ? "":permission);
                    }
                    case COST -> {
                        if (Ari.ECONOMY_SERVICE.isNull()) {
                            item.setName(Ari.instance.getConfigInstance().getValue("server.message.no-economy", LangFile.LANG));
                            item.setMaterial("barrier");
                        } else {
                            Double cost = this.warp.getCost();
                            item.setName(cost == null ? "":cost.toString());
                        }
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT, Ari.DATA_SERVICE.getValue(this.warp.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
                }
            }
        }
    }

    @Override
    protected void whenRenderComplete(@NotNull Inventory inventory) {

    }

    @Override
    protected void onClose() {
        this.warp = null;
    }

}
