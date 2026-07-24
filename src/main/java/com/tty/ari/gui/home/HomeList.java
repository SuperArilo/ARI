package com.tty.ari.gui.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.ComponentTool;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.PageResult;
import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.gui.BaseDataItemConfigInventory;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.home.HomeGuiConfig;
import com.tty.ari.entity.ServerHome;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
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
                        new LambdaQueryWrapper<>(ServerHome.class)
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
                textComponents.add(ComponentTool.text(line, types));
            }

            this.getPlugin().getNbtManager().setNbt(NbtGuiValue.GUI_DATA_ID, itemStack, PersistentDataType.STRING, ph.getHomeId());
            this.getPlugin().getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, FunctionType.DATA.getName());

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentTool.text(ph.getHomeName(), this.getOfflinePlayer()));
            itemMeta.lore(textComponents);

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
        return Ari.instance.getConfigurationManager().get(HomeGuiConfig.class).getMenuConfig(BaseDataMenu.class);
    }

    @Override
    protected void beforeRenderMasksAsync(@Nullable Mask mask) {}

    @Override
    protected void beforeRenderFunctionItemsAsync(@Nullable Map<String, FunctionItems> functionItems) {}

}
