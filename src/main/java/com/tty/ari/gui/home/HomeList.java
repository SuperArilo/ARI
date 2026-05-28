package com.tty.ari.gui.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.ari.Ari;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.PageResult;
import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.enumType.FilePath;
import com.tty.api.gui.BaseDataItemConfigInventory;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.utils.FormatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "home_list")
public class HomeList extends BaseDataItemConfigInventory<ServerHome> {

    public HomeList(Player player) {
        super(Ari.instance, player);
    }

    @Override
    public CompletableFuture<PageResult<ServerHome>> requestData() {
        return Ari.REPOSITORY_MANAGER.get(ServerHome.class)
                .getList(
                        this.pageNum,
                        ((BaseDataMenu)this.getBaseMenu()).getDataItems().getSlot().size(),
                        new LambdaQueryWrapper<ServerHome>()
                                .eq(ServerHome::getPlayerUUID, this.getOfflinePlayer().getUniqueId().toString())
                                .orderByDesc(ServerHome::isTopSlot),
                        PartitionKey.of(this.getOfflinePlayer().getUniqueId().toString())
                );
    }

    @Override
    protected @NotNull List<ItemStack> beforeRenderDataItem(List<ServerHome> data) {
        List<ItemStack> list = new ArrayList<>();

        List<String> rawLore = ((BaseDataMenu) this.getBaseMenu()).getDataItems().getLore();

        for (ServerHome ph : data) {
            ItemStack itemStack = this.createItemStack(ph.getShowMaterial());
            if (itemStack == null) {
                Ari.instance.getLog().error("There is a problem with the homeID: [{}] of the player: [{}]", ph.getHomeId(), this.getOfflinePlayer().getName());
                Ari.instance.getLog().warn("Skip the rendering homeId [{}] process...", ph.getHomeId());
                if (this.getOfflinePlayer() instanceof Player player) {
                    player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
                }
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
                textComponents.add(ComponentUtils.text(line, types));
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentUtils.text(ph.getHomeName(), this.getOfflinePlayer()));
            itemMeta.lore(textComponents);

            this.setNBT(itemMeta, GuiNBTKeys.GUI_RENDER_DATA_ID, PersistentDataType.STRING, ph.getHomeId());
            this.setNBT(itemMeta, GuiNBTKeys.GUI_RENDER_FUNCTION_ICON, PersistentDataType.STRING, FunctionType.DATA.name());

            if (ph.isTopSlot()) {
                this.setHighlight(itemMeta);
            }
            itemStack.setItemMeta(itemMeta);
            list.add(itemStack);
        }
        return list;
    }

    @Override
    protected @NotNull BaseDataMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.HOME_LIST_GUI.name()).saveToString(), BaseDataMenu.class);
    }

    @Override
    protected void beforeRenderMasks(@Nullable Mask mask) {

    }

    @Override
    protected void beforeRenderFunctionItems(Map<String, FunctionItems> functionItems) {

    }

}
