package com.tty.gui.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.api.FormatUtils;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.PageResult;
import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.GuiType;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.api.gui.BaseDataItemConfigInventory;
import com.tty.api.Log;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = GuiType.WARP_LIST)
public class WarpList extends BaseDataItemConfigInventory<ServerWarp> {

    private String baseFree = Ari.DATA_SERVICE.getValue("base.free");

    public WarpList(Player player) {
        super(Ari.instance, player,  Ari.COMPONENT_SERVICE);
    }

    @Override
    public CompletableFuture<PageResult<ServerWarp>> requestData() {
        return Ari.REPOSITORY_MANAGER.get(ServerWarp.class).getList(this.pageNum, ((BaseDataMenu) this.getBaseMenu()).getDataItems().getSlot().size(), new LambdaQueryWrapper<>(ServerWarp.class));
    }

    @Override
    protected Map<Integer, ItemStack> getRenderItem() {
        Map<Integer, ItemStack> map = new HashMap<>();
        BaseDataMenu baseDataMenu = (BaseDataMenu) this.getBaseMenu();
        List<Integer> dataSlot = baseDataMenu.getDataItems().getSlot();
        List<String> rawLore = baseDataMenu.getDataItems().getLore();

        for (int i = 0; i < this.data.size(); i++) {
            ServerWarp serverWarp = this.data.get(i);

            ItemStack itemStack = this.createItemStack(serverWarp.getShowMaterial());
            if (itemStack == null) {
                Log.warn("There is a problem with the warpID: [{}] of the player: [{}]", serverWarp.getWarpId(), this.player.getName());
                Log.error("Skip the rendering warpId [{}] process...", serverWarp.getWarpId());
                this.player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
                continue;
            }

            List<TextComponent> textComponents = new ArrayList<>();
            Location location = FormatUtils.parseLocation(serverWarp.getLocation());

            UUID uuid = UUID.fromString(serverWarp.getCreateBy());
            String playName;
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null) {
                playName = onlinePlayer.getName();
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                playName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "";
            }

            boolean hasPermission = serverWarp.getPermission() == null ||
                    serverWarp.getPermission().isEmpty() ||
                    Ari.PERMISSION_SERVICE.hasPermission(this.player, serverWarp.getPermission()) ||
                    UUID.fromString(serverWarp.getCreateBy()).equals(this.player.getUniqueId());

            Map<String, Component> types = new HashMap<>();
            types.put(IconKeyType.ID.getKey(), Component.text(serverWarp.getId()));
            types.put(IconKeyType.X.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
            types.put(IconKeyType.Y.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
            types.put(IconKeyType.Z.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
            types.put(IconKeyType.WORLD_NAME.getKey(), Component.text(location.getWorld().getName()));
            types.put(IconKeyType.PLAYER_NAME.getKey(), Ari.COMPONENT_SERVICE.text(playName));
            Double cost = serverWarp.getCost();
            types.put(IconKeyType.COST.getKey(), Ari.COMPONENT_SERVICE.text(cost == null || cost == 0 || Ari.ECONOMY_SERVICE.isNull() ? baseFree : cost + Ari.ECONOMY_SERVICE.getNamePlural()));
            types.put(IconKeyType.TOP_SLOT.getKey(), Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue(serverWarp.isTopSlot() ? "base.yes_re":"base.no_re")));
            types.put(IconKeyType.PERMISSION.getKey(), Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue(hasPermission ? "base.yes_re":"base.no_re")));


            for (String s : rawLore) {
                textComponents.add(Ari.COMPONENT_SERVICE.text(s, types));
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Ari.COMPONENT_SERVICE.text(serverWarp.getWarpName(), this.player));
            itemMeta.lore(textComponents);

            this.setNBT(itemMeta, "warp_id", PersistentDataType.STRING, serverWarp.getWarpId());
            this.setNBT(itemMeta, "type", PersistentDataType.STRING, FunctionType.DATA.name());

            if (serverWarp.isTopSlot()) {
                this.setHighlight(itemMeta);
            }
            itemStack.setItemMeta(itemMeta);
            map.put(dataSlot.get(i), itemStack);
        }

        return map;
    }

    @Override
    protected @NotNull BaseMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.WARP_LIST_GUI.name()).saveToString(), BaseDataMenu.class);
    }

    @Override
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        return null;
    }

    @Override
    public void clean() {
        super.clean();
        this.baseFree = null;
    }

}
