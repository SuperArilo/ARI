package com.tty.gui.warp;

import com.tty.Ari;
import com.tty.lib.dto.PageResult;
import com.tty.lib.entity.gui.BaseDataMenu;
import com.tty.lib.entity.gui.FunctionItems;
import com.tty.lib.entity.gui.Mask;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.function.WarpManager;
import com.tty.lib.gui.BaseDataItemConfigInventory;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WarpList extends BaseDataItemConfigInventory<ServerWarp> {

    private final String baseFree = Ari.instance.dataService.getValue("base.free");

    public WarpList(Player player) {
        super(Ari.instance,
                FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.WARP_LIST_GUI.name()).saveToString(), BaseDataMenu.class),
                player,
                GuiType.WARP_LIST);
    }

    @Override
    public CompletableFuture<PageResult<ServerWarp>> requestData() {
        return Ari.REPOSITORY_MANAGER.get(ServerWarp.class).getList(this.pageNum, this.pageSize, new WarpManager.QueryKey(null, null));
    }

    @Override
    protected Map<Integer, ItemStack> getRenderItem() {
        Map<Integer, ItemStack> map = new HashMap<>();
        List<Integer> dataSlot = this.baseDataInstance.getDataItems().getSlot();
        List<String> rawLore = this.baseDataInstance.getDataItems().getLore();

        for (int i = 0; i < this.data.size(); i++) {
            ServerWarp serverWarp = this.data.get(i);

            ItemStack itemStack = this.createItemStack(serverWarp.getShowMaterial());
            if (itemStack == null) {
                Log.warn("There is a problem with the warpID: [{}] of the player: [{}]", serverWarp.getWarpId(), this.player.getName());
                Log.error("Skip the rendering warpId [{}] process...", serverWarp.getWarpId());
                this.player.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
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
                    PermissionUtils.hasPermission(this.player, serverWarp.getPermission()) ||
                    UUID.fromString(serverWarp.getCreateBy()).equals(this.player.getUniqueId());

            Map<String, Component> types = new HashMap<>();
            types.put(IconKeyType.ID.getKey(), Component.text(serverWarp.getId()));
            types.put(IconKeyType.X.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
            types.put(IconKeyType.Y.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
            types.put(IconKeyType.Z.getKey(), Component.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
            types.put(IconKeyType.WORLD_NAME.getKey(), Component.text(location.getWorld().getName()));
            types.put(IconKeyType.PLAYER_NAME.getKey(), ComponentUtils.text(playName));
            Double cost = serverWarp.getCost();
            types.put(IconKeyType.COST.getKey(), ComponentUtils.text(cost == null || cost == 0 || EconomyUtils.isNull() ? baseFree : cost + EconomyUtils.getNamePlural()));
            types.put(IconKeyType.TOP_SLOT.getKey(), ComponentUtils.text(Ari.instance.dataService.getValue(serverWarp.isTopSlot() ? "base.yes_re":"base.no_re")));
            types.put(IconKeyType.PERMISSION.getKey(), ComponentUtils.text(Ari.instance.dataService.getValue(hasPermission ? "base.yes_re":"base.no_re")));


            for (String s : rawLore) {
                textComponents.add(ComponentUtils.text(s, types));
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentUtils.text(serverWarp.getWarpName(), this.player));
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
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        return null;
    }

}
