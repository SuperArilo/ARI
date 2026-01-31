package com.tty.gui.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.PageResult;
import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.enumType.GuiType;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.api.gui.BaseDataItemConfigInventory;
import com.tty.api.Log;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.FormatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "home_list")
public class HomeList extends BaseDataItemConfigInventory<ServerHome> {

    public HomeList(Player player) {
        super(Ari.instance, player, Ari.COMPONENT_SERVICE);
    }

    @Override
    public CompletableFuture<PageResult<ServerHome>> requestData() {
        return Ari.REPOSITORY_MANAGER.get(ServerHome.class).getList(this.pageNum, ((BaseDataMenu)this.getBaseMenu()).getDataItems().getSlot().size(), new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getPlayerUUID, this.player.getUniqueId().toString()));
    }

    @Override
    protected Map<Integer, ItemStack> getRenderItem() {
        Map<Integer, ItemStack> map = new HashMap<>();
        BaseDataMenu baseDataMenu = (BaseDataMenu) this.getBaseMenu();
        List<Integer> dataSlot = baseDataMenu.getDataItems().getSlot();
        List<String> rawLore = baseDataMenu.getDataItems().getLore();
        for (int i = 0; i < this.data.size(); i++) {
            ServerHome ph = this.data.get(i);
            ItemStack itemStack = this.createItemStack(ph.getShowMaterial());
            if (itemStack == null) {
                Log.error("There is a problem with the homeID: [{}] of the player: [{}]", ph.getHomeId(), this.player.getName());
                Log.warn("Skip the rendering homeId [{}] process...", ph.getHomeId());
                this.player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
                continue;
            }

            List<TextComponent> textComponents = new ArrayList<>();
            Location location = FormatUtils.parseLocation(ph.getLocation());

            Map<String, Component> types = new HashMap<>();
            types.put(IconKeyType.ID.getKey(), Component.text(ph.getHomeId()));
            types.put(IconKeyType.X.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
            types.put(IconKeyType.Y.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
            types.put(IconKeyType.Z.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
            types.put(IconKeyType.WORLD_NAME.getKey(), Component.text(location.getWorld().getName()));

            for (String line : rawLore) {
                textComponents.add(Ari.COMPONENT_SERVICE.text(line, types));
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Ari.COMPONENT_SERVICE.text(ph.getHomeName(), this.player));
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
    protected @NotNull BaseDataMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.HOME_LIST_GUI.name()).saveToString(), BaseDataMenu.class);
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
