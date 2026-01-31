package com.tty.gui.home;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.enumType.GuiType;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.FormatUtils;
import com.tty.api.PublicFunctionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@GuiMeta(type = "home_edit")
public class HomeEditor extends BaseConfigInventory {

    public ServerHome currentHome;

    public HomeEditor(ServerHome serverHome, Player player) {
        super(Ari.instance, player, Ari.COMPONENT_SERVICE);
        this.currentHome = serverHome;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.HOME_EDIT_GUI.name()).saveToString(), BaseMenu.class);
    }

    @Override
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        Map<String, FunctionItems> functionItems = PublicFunctionUtils.deepCopy(this.getBaseMenu().getFunctionItems(), new TypeToken<Map<String, FunctionItems>>(){}.getType());
        if (functionItems != null) {
            for (FunctionItems item : functionItems.values()) {
                switch (item.getType()) {
                    case ICON -> item.setMaterial(this.currentHome.getShowMaterial());
                    case RENAME -> item.setName(this.currentHome.getHomeName());
                    case LOCATION -> {
                        Location location = FormatUtils.parseLocation(this.currentHome.getLocation());
                        Map<String, String> m = new HashMap<>();
                        m.put(IconKeyType.X.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.DATA_SERVICE.getValue(this.currentHome.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
                }
            }
        }
        return functionItems;
    }

    @Override
    public void clean() {
        this.currentHome = null;
    }

}
