package com.tty.ari.gui.home;

import com.google.common.reflect.TypeToken;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.home.HomeEditConfig;
import com.tty.ari.entity.ServerHome;
import lombok.Getter;
import org.bukkit.Bukkit;
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
        return Ari.instance.getConfigurationManager().get(HomeEditConfig.class).getMenuConfig(BaseMenu.class);
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
                        String strLocation = this.home.getLocation();
                        Location location;
                        try {
                            Map<String, Object> o = this.getPlugin().getConfigurationManager().convertTo(strLocation, new TypeToken<Map<String, Object>>() {}.getType());
                            if (o == null) {
                                location = FormatUtils.parseLocation(strLocation);
                            } else {
                                location = Location.deserialize(o);
                            }
                        } catch (Exception e) {
                            this.getPlugin().getLog().error(e);
                            if (this.getOfflinePlayer() instanceof Player player) {
                                player.sendMessage(this.getPlugin().getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error"), player));
                                location = new Location(player.getWorld(), 0, 0, 0);
                            } else {
                                location = new Location(Bukkit.getWorlds().getFirst(), 0, 0 ,0);
                            }
                        }
                        Map<IconKeyType, String> m = new HashMap<>();
                        m.put(IconKeyType.X, FormatUtils.formatTwoDecimalPlaces(location.getX()));
                        m.put(IconKeyType.Y, FormatUtils.formatTwoDecimalPlaces(location.getY()));
                        m.put(IconKeyType.Z, FormatUtils.formatTwoDecimalPlaces(location.getZ()));
                        item.setName(this.replaceKey(item.getName(), m));
                    }
                    case TOP_SLOT -> item.setLore(item.getLore().stream().map(lore -> this.replaceKey(lore, Map.of(IconKeyType.TOP_SLOT, Ari.DATA_SERVICE.getValue(this.home.isTopSlot() ? "base.yes_re":"base.no_re")))).toList());
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
