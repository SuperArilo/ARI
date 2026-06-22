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

@GuiMeta(type = "home_edit")
public class HomeEditor extends BaseConfigInventory {

    @Getter
    private volatile ServerHome home;

    public HomeEditor(Player player, ServerHome serverHome) {
        super(Ari.instance, player);
        this.home = serverHome;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return Ari.instance.getConfigInstance().yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.HOME_EDIT_GUI.name()).saveToString(), BaseMenu.class);
    }

    @Override
    protected void beforeRenderMasksAsync(@Nullable Mask mask) {}

    @Override
    protected void beforeRenderFunctionItemsAsync(Map<String, FunctionItems> functionItems) {
        if (functionItems != null) {
            for (FunctionItems item : functionItems.values()) {
                switch (item.getType()) {
                    case ICON -> item.setMaterial(this.home.getShowMaterial());
                    case RENAME -> item.setName(this.home.getHomeName());
                    case LOCATION -> {
                        Location location = FormatUtils.parseLocation(this.home.getLocation());
                        Map<String, String> m = new HashMap<>();
                        m.put(IconKeyType.X.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z.getKey(), FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.DATA_SERVICE.getValue(this.home.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
                }
            }
        }
    }

    @Override
    protected void whenRenderComplete(@NotNull Inventory inventory) {

    }

    @Override
    protected void onClose() {
        this.home = null;
    }

}
