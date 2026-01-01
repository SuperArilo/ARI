package com.tty.gui.warp;

import com.tty.Ari;
import com.tty.lib.entity.gui.BaseDataMenu;
import com.tty.lib.entity.gui.FunctionItems;
import com.tty.lib.entity.gui.Mask;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.function.WarpManager;
import com.tty.lib.gui.BaseDataItemInventory;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.EconomyUtils;
import com.tty.lib.tool.PermissionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WarpList extends BaseDataItemInventory<ServerWarp> {

    private final String baseFree = Ari.instance.dataService.getValue("base.free");

    public WarpList(Player player) {
        super(Ari.instance,
                FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.WARP_LIST_GUI.name()).saveToString(), BaseDataMenu.class),
                player,
                GuiType.WARP_LIST);
    }

    @Override
    public CompletableFuture<List<ServerWarp>> requestData() {
        return new WarpManager(true).getList(this.pageNum, this.pageSize);
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
                Log.warn("There is a problem with the warpID: [%s] of the player: [%s]", serverWarp.getWarpId(), this.player.getName());
                Log.error("Skip the rendering warpId [%s] process...", serverWarp.getWarpId());
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

            for (String line : rawLore) {
                Map<String, Component> replacements = new HashMap<>();

                for (IconKeyType keyType : IconKeyType.values()) {
                    switch (keyType) {
                        case ID -> replacements.put(keyType.getKey(), ComponentUtils.text(serverWarp.getWarpId()));
                        case X -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
                        case Y -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
                        case Z -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
                        case WORLD_NAME -> replacements.put(keyType.getKey(), ComponentUtils.text(location.getWorld().getName()));
                        case PLAYER_NAME -> replacements.put(keyType.getKey(), ComponentUtils.text(playName));
                        case COST -> {
                            Double cost = serverWarp.getCost();
                            replacements.put(keyType.getKey(), ComponentUtils.text(cost == null || cost == 0 || EconomyUtils.isNull() ? baseFree : cost + EconomyUtils.getNamePlural()));
                        }
                        case TOP_SLOT -> replacements.put(keyType.getKey(), ComponentUtils.text(Ari.instance.dataService.getValue(serverWarp.isTopSlot() ? "base.yes_re":"base.no_re")));
                        case PERMISSION -> replacements.put(keyType.getKey(), ComponentUtils.text(Ari.instance.dataService.getValue(hasPermission ? "base.yes_re":"base.no_re")));
                    }
                }

                textComponents.add(ComponentUtils.text(line, replacements));
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
