package com.tty.listener.warp;

import com.tty.Ari;
import com.tty.dto.state.teleport.EntityToLocationCallbackState;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.function.WarpManager;
import com.tty.gui.warp.WarpEditor;
import com.tty.gui.warp.WarpList;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.enum_type.TeleportType;
import com.tty.lib.tool.EconomyUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PermissionUtils;
import com.tty.listener.BaseGuiListener;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;

public class WarpListListener extends BaseGuiListener {

    private final WarpManager warpManager = new WarpManager(true);
    private final NamespacedKey TYPE_KEY = new NamespacedKey(Ari.instance, "type");
    private final NamespacedKey WARP_ID_KEY = new NamespacedKey(Ari.instance, "warp_id");

    public WarpListListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        assert currentItem != null;
        Inventory inventory = event.getInventory();
        WarpList warpList = (WarpList) inventory.getHolder();
        assert warpList != null;

        FunctionType type = FormatUtils.ItemNBT_TypeCheck(currentItem.getItemMeta().getPersistentDataContainer().get(this.TYPE_KEY, PersistentDataType.STRING));
        if(type == null) return;
        Player player = warpList.player;

        switch (type) {
            case BACK -> inventory.close();
            case DATA -> {
                String warpId = currentItem.getItemMeta().getPersistentDataContainer().get(this.WARP_ID_KEY, PersistentDataType.STRING);
                //从数据库查询最新的
                this.warpManager.getInstance(warpId).thenAccept((instance) -> {
                    if (instance == null) {
                        Log.error("can't find warpId: %s", warpId);
                        return;
                    }
                    boolean isOwner = UUID.fromString(instance.getCreateBy()).equals(player.getUniqueId());
                    ClickType eventClick = event.getClick();
                    switch (eventClick) {
                        case LEFT -> {
                            Location targetLocation = FormatUtils.parseLocation(instance.getLocation());
                            Ari.instance.stateMachineManager
                                    .get(TeleportStateService.class)
                                    .addState(new EntityToLocationCallbackState(
                                            player,
                                            Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.WARP_CONFIG, Integer.class, 3),
                                            targetLocation,
                                            () -> {
                                                String permission = instance.getPermission();
                                                if(permission != null && !permission.isEmpty()) {
                                                    boolean hasPermission = PermissionUtils.hasPermission(player, permission);
                                                    if (!hasPermission && !isOwner) {
                                                        player.sendMessage(ConfigUtils.t("function.warp.no-permission-teleport"));
                                                        return false;
                                                    }
                                                }
                                                if(!EconomyUtils.hasEnoughBalance(player, instance.getCost()) && !isOwner &&
                                                        Ari.C_INSTANCE.getValue("main.permission", FilePath.WARP_CONFIG, Boolean.class, true)) {
                                                    player.sendMessage(ConfigUtils.t("function.warp.not-enough-money"));
                                                    return false;
                                                }
                                                return true;
                                            },
                                            () -> {
                                                //判断是否是地标拥有者或者是不是op，如果是则不扣
                                                if(!isOwner &&
                                                        !player.isOp() &&
                                                        Ari.C_INSTANCE.getValue("main.cost", FilePath.WARP_CONFIG, Boolean.class, false) &&
                                                        !EconomyUtils.isNull()) {
                                                    EconomyUtils.withdrawPlayer(player, instance.getCost());
                                                    player.sendMessage(ConfigUtils.t("teleport.costed", Map.of(LangType.COSTED.getType(), Component.text(instance.getCost().toString() + EconomyUtils.getNamePlural()))));
                                                }
                                            },
                                            TeleportType.WARP));
                            Lib.Scheduler.runAtEntity(Ari.instance, player, i -> inventory.close(), null);
                        }
                        case RIGHT -> {
                            if(isOwner || player.isOp()) {
                                Lib.Scheduler.run(Ari.instance, i -> {
                                    inventory.close();
                                    new WarpEditor(instance, player).open();
                                });
                            } else {
                                player.sendMessage(ConfigUtils.t("function.warp.no-permission-edit"));
                            }
                        }
                    }
                }).exceptionally(i -> {
                   Log.error(i, "get warp id %s error", warpId);
                   return null;
                });
            }
            case PREV -> warpList.prev();
            case NEXT -> warpList.next();
        }
    }
}
