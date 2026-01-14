package com.tty.gui.home;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.lib.entity.gui.BaseMenu;
import com.tty.lib.entity.gui.FunctionItems;
import com.tty.lib.entity.gui.Mask;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.lib.gui.BaseConfigInventory;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HomeEditor extends BaseConfigInventory {

    public ServerHome currentHome;

    public HomeEditor(ServerHome serverHome, Player player) {
        super(Ari.instance,
                FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.HOME_EDIT_GUI.name()).saveToString(), BaseMenu.class),
                player,
                GuiType.HOME_EDIT);
        this.currentHome = serverHome;
    }

    @Override
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        Map<String, FunctionItems> functionItems = PublicFunctionUtils.deepCopy(this.baseInstance.getFunctionItems(), new TypeToken<Map<String, FunctionItems>>(){}.getType());
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
    protected void beforeOpen() {

    }

    @Override
    public void clean() {
        super.clean();
        this.currentHome = null;
    }

}
