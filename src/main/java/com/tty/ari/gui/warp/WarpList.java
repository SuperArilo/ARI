package com.tty.ari.gui.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.PageResult;
import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.gui.BaseDataItemConfigInventory;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.entity.ServerWarp;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.tool.PlayerNameCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "warp_list")
public class WarpList extends BaseDataItemConfigInventory<ServerWarp> {

    private String baseFree = Ari.DATA_SERVICE.getValue("base.free");

    public WarpList(Player player) {
        super(Ari.instance, player);
    }

    @Override
    public CompletableFuture<PageResult<ServerWarp>> requestData() {
        return Ari.REPOSITORY_MANAGER.get(ServerWarp.class)
                .getList(
                        this.pageNum,
                        ((BaseDataMenu) this.getBaseMenu()).getDataItems().getSlot().size(),
                        new LambdaQueryWrapper<>(ServerWarp.class)
                                .orderByDesc(ServerWarp::isTopSlot),
                        PartitionKey.global()
                );
    }

    @Override
    protected @NotNull List<ItemStack> beforeRenderDataItem(List<ServerWarp> data) {
        List<ItemStack> list = new ArrayList<>();
        List<String> rawLore = ((BaseDataMenu) this.getBaseMenu()).getDataItems().getLore();

        for (ServerWarp serverWarp : data) {
            ItemStack itemStack = this.createItemStack(serverWarp.getShowMaterial());
            if (itemStack == null) {
                Ari.instance.getLog().warn("There is a problem with the warpID: [{}] of the player: [{}]", serverWarp.getWarpId(), this.getOfflinePlayer().getName());
                Ari.instance.getLog().error("Skip the rendering warpId [{}] process...", serverWarp.getWarpId());
                if (this.getOfflinePlayer() instanceof Player player) {
                    player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
                }
                continue;
            }

            List<TextComponent> textComponents = new ArrayList<>();
            Location location = FormatUtils.parseLocation(serverWarp.getLocation());

            boolean hasPermission = serverWarp.getPermission() == null ||
                    serverWarp.getPermission().isEmpty() ||
                    Ari.PERMISSION_SERVICE.hasPermission((Player) this.getOfflinePlayer(), serverWarp.getPermission()) ||
                    UUID.fromString(serverWarp.getCreateBy()).equals(this.getOfflinePlayer().getUniqueId());

            Map<String, Component> types = new HashMap<>();
            types.put(IconKeyType.ID.getKey(), Component.text(serverWarp.getWarpId()));
            types.put(IconKeyType.X.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
            types.put(IconKeyType.Y.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
            types.put(IconKeyType.Z.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
            types.put(IconKeyType.WORLD_NAME.getKey(), Component.text(location.getWorld().getName()));
            types.put(IconKeyType.PLAYER_NAME.getKey(), Ari.instance.getComponentTool().text(PlayerNameCache.getName(UUID.fromString(serverWarp.getCreateBy()))));
            Double cost = serverWarp.getCost();
            types.put(IconKeyType.COST.getKey(), Ari.instance.getComponentTool().text(cost == null || cost == 0 || Ari.ECONOMY_SERVICE.isNull() ? baseFree : cost + Ari.ECONOMY_SERVICE.getNamePlural()));
            types.put(IconKeyType.TOP_SLOT.getKey(), Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue(serverWarp.isTopSlot() ? "base.yes_re":"base.no_re")));
            types.put(IconKeyType.PERMISSION.getKey(), Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue(hasPermission ? "base.yes_re":"base.no_re")));


            for (String s : rawLore) {
                textComponents.add(Ari.instance.getComponentTool().text(s, types));
            }

            this.getPlugin().getNbtManager().setNbt(NbtGuiValue.GUI_DATA_ID, itemStack, PersistentDataType.STRING, serverWarp.getWarpId());
            this.getPlugin().getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, FunctionType.DATA.getName());

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Ari.instance.getComponentTool().text(serverWarp.getWarpName(), this.getOfflinePlayer()));
            itemMeta.lore(textComponents);

            if (serverWarp.isTopSlot()) {
                this.setHighlight(itemMeta);
            }
            itemStack.setItemMeta(itemMeta);
            list.add(itemStack);
        }
        return list;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return Ari.instance.getConfigInstance().yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.WARP_LIST_GUI).saveToString(), BaseDataMenu.class);
    }

    @Override
    protected void beforeRenderMasksAsync(@Nullable Mask mask) {
    }

    @Override
    protected void beforeRenderFunctionItemsAsync(@Nullable Map<String, FunctionItems> functionItems) {
    }

    @Override
    protected void onClose() {
        super.onClose();
        this.baseFree = null;
    }

}
