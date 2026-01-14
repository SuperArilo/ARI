package com.tty.gui.home;

import com.tty.Ari;
import com.tty.function.HomeManager;
import com.tty.lib.dto.PageResult;
import com.tty.lib.entity.gui.BaseDataMenu;
import com.tty.lib.entity.gui.FunctionItems;
import com.tty.lib.entity.gui.Mask;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.lib.gui.BaseDataItemConfigInventory;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeList extends BaseDataItemConfigInventory<ServerHome> {

    public HomeList(Player player) {
        super(Ari.instance,
                FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.HOME_LIST_GUI.name()).saveToString(), BaseDataMenu.class),
                player,
                GuiType.HOME_LIST);
    }

    @Override
    public CompletableFuture<PageResult<ServerHome>> requestData() {
        int size = this.baseDataInstance.getDataItems().getSlot().size();
        return Ari.REPOSITORY_MANAGER.get(ServerHome.class).getList(this.pageNum, size, new HomeManager.QueryKey(this.player.getUniqueId().toString(), null));
    }

    @Override
    protected Map<Integer, ItemStack> getRenderItem() {
        Map<Integer, ItemStack> map = new HashMap<>();
        List<Integer> dataSlot = this.baseDataInstance.getDataItems().getSlot();
        List<String> rawLore = this.baseDataInstance.getDataItems().getLore();
        for (int i = 0; i < this.data.size(); i++) {
            ServerHome ph = this.data.get(i);
            ItemStack itemStack = this.createItemStack(ph.getShowMaterial());
            if (itemStack == null) {
                Log.error("There is a problem with the homeID: [%s] of the player: [%s]", ph.getHomeId(), this.player.getName());
                Log.warn("Skip the rendering homeId [%s] process...", ph.getHomeId());
                this.player.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
                continue;
            }

            List<TextComponent> textComponents = new ArrayList<>();
            Location location = FormatUtils.parseLocation(ph.getLocation());

            for (String line : rawLore) {
                Map<String, Component> replacements = new HashMap<>();

                for (IconKeyType keyType : IconKeyType.values()) {
                    switch (keyType) {
                        case ID -> replacements.put(keyType.getKey(), ComponentUtils.text(ph.getHomeId()));
                        case X -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
                        case Y -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
                        case Z -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
                        case WORLD_NAME -> replacements.put(keyType.getKey(), ComponentUtils.text(location.getWorld().getName()));
                    }
                }
                textComponents.add(ComponentUtils.text(line, replacements));
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentUtils.text(ph.getHomeName(), this.player));
            itemMeta.lore(textComponents);

            this.setNBT(itemMeta, "home_id", PersistentDataType.STRING, ph.getHomeId());
            this.setNBT(itemMeta, "type", PersistentDataType.STRING, FunctionType.DATA.name());

            if (ph.isTopSlot()) {
                this.setHighlight(itemMeta);
            }
            itemStack.setItemMeta(itemMeta);
            map.put(dataSlot.get(i), itemStack);
        }
        return map;
    }

    @Override
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        return null;
    }

}
