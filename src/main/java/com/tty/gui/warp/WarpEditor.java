package com.tty.gui.warp;

import com.tty.Ari;
import com.tty.dto.CustomInventoryHolder;
import com.tty.entity.menu.BaseMenu;
import com.tty.entity.menu.FunctionItems;
import com.tty.entity.menu.Mask;
import com.tty.entity.sql.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.gui.BaseInventory;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.lib.tool.EconomyUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WarpEditor extends BaseInventory {

    public final ServerWarp currentWarp;

    public WarpEditor(ServerWarp serverWarp, Player player) {
        super(FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.WARP_EDIT_GUI.name()).saveToString(), BaseMenu.class), player);
        this.currentWarp = serverWarp;
    }

    @Override
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        Map<String, FunctionItems> functionItems = PublicFunctionUtils.deepCopyBySerialization(this.baseInstance.getFunctionItems());
        if(functionItems != null) {
            for (FunctionItems item : functionItems.values()) {
                switch (item.getType()) {
                    case ICON -> item.setMaterial(this.currentWarp.getShowMaterial());
                    case RENAME -> item.setName(this.currentWarp.getWarpName());
                    case LOCATION -> {
                        Location location = FormatUtils.parseLocation(this.currentWarp.getLocation());
                        Map<String, String> m = new HashMap<>();
                        m.put(IconKeyType.X.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case PERMISSION -> {
                        String permission = this.currentWarp.getPermission();
                        item.setName(permission == null ? "":permission);
                    }
                    case COST -> {
                        if (EconomyUtils.isNull()) {
                            item.setName(Ari.C_INSTANCE.getValue("server.message.no-economy", FilePath.LANG));
                            item.setMaterial("barrier");
                        } else {
                            Double cost = this.currentWarp.getCost();
                            item.setName(cost == null ? "":cost.toString());
                        }
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.instance.dataService.getValue(this.currentWarp.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
                }
            }
        }
        return functionItems;
    }

    @Override
    protected CustomInventoryHolder createHolder() {
        return new CustomInventoryHolder(player, this.inventory, GuiType.WARP_EDIT, this);
    }

}
