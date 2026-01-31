package com.tty.listener.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.dto.state.teleport.EntityToLocationCallbackState;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.enumType.lang.LangVault;
import com.tty.enumType.GuiType;
import com.tty.gui.warp.WarpEditor;
import com.tty.gui.warp.WarpList;
import com.tty.api.enumType.FunctionType;
import com.tty.enumType.TeleportType;
import com.tty.api.FormatUtils;
import com.tty.api.listener.BaseGuiListener;
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

        FunctionType type = this.ItemNBT_TypeCheck(currentItem.getItemMeta().getPersistentDataContainer().get(this.TYPE_KEY, PersistentDataType.STRING));
        if(type == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        switch (type) {
            case BACK -> inventory.close();
            case DATA -> {
                String warpId = currentItem.getItemMeta().getPersistentDataContainer().get(this.WARP_ID_KEY, PersistentDataType.STRING);
                //从数据库查询最新的
                Ari.REPOSITORY_MANAGER.get(ServerWarp.class).get(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpId)).thenAccept((instance) -> {
                    if (instance == null) {
                        Ari.LOG.error("can't find warpId: {}", warpId);
                        return;
                    }
                    boolean isOwner = UUID.fromString(instance.getCreateBy()).equals(player.getUniqueId());
                    ClickType eventClick = event.getClick();
                    switch (eventClick) {
                        case LEFT -> {
                            Location targetLocation = FormatUtils.parseLocation(instance.getLocation());
                            Ari.STATE_MACHINE_MANAGER
                                .get(TeleportStateService.class)
                                .addState(new EntityToLocationCallbackState(
                                    player,
                                    Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.WARP_CONFIG, Integer.class, 3),
                                    targetLocation,
                                    () -> {
                                        String permission = instance.getPermission();
                                        if(permission != null && !permission.isEmpty()) {
                                            boolean hasPermission = Ari.PERMISSION_SERVICE.hasPermission(player, permission);
                                            if (!hasPermission && !isOwner) {
                                                ConfigUtils.t("function.warp.no-permission-teleport", player).thenAccept(player::sendMessage);
                                                return false;
                                            }
                                        }
                                        if(!Ari.ECONOMY_SERVICE.hasEnoughBalance(player, instance.getCost()) && !isOwner &&
                                                Ari.C_INSTANCE.getValue("main.permission", FilePath.WARP_CONFIG, Boolean.class, true)) {
                                            ConfigUtils.t("function.warp.not-enough-money", player).thenAccept(player::sendMessage);
                                            return false;
                                        }
                                        return true;
                                    },
                                    () -> {
                                        //判断是否是地标拥有者或者是不是op，如果是则不扣
                                        if(!isOwner &&
                                                !player.isOp() &&
                                                Ari.C_INSTANCE.getValue("main.cost", FilePath.WARP_CONFIG, Boolean.class, false) &&
                                                !Ari.ECONOMY_SERVICE.isNull()) {
                                            Ari.ECONOMY_SERVICE.withdrawPlayer(player, instance.getCost());
                                            player.sendMessage(ConfigUtils.tAfter("teleport.costed", Map.of(LangVault.COSTED_UNRESOLVED.getType(), Component.text(instance.getCost().toString() + Ari.ECONOMY_SERVICE.getNamePlural()))));
                                        }
                                    },
                                    TeleportType.WARP));
                            Ari.SCHEDULER.runAtEntity(Ari.instance, player, i -> inventory.close(), null);
                        }
                        case RIGHT -> {
                            if(isOwner || player.isOp()) {
                                Ari.SCHEDULER.run(Ari.instance, i -> {
                                    inventory.close();
                                    player.openInventory(new WarpEditor(instance, player).getInventory());
                                });
                            } else {
                                ConfigUtils.t("function.warp.no-permission-edit", player).thenAccept(player::sendMessage);
                            }
                        }
                    }
                }).exceptionally(i -> {
                    Ari.LOG.error(i, "get warp id {} error", warpId);
                   return null;
                });
            }
            case PREV_PAGE -> warpList.prev();
            case NEXT_PAGE -> warpList.next();
        }
    }
}
