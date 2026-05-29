package com.tty.ari.listener.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.ari.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.FunctionType;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.ari.dto.state.teleport.EntityToLocationCallbackState;
import com.tty.ari.entity.ServerWarp;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.enumType.lang.LangVault;
import com.tty.ari.gui.warp.WarpEditor;
import com.tty.ari.gui.warp.WarpList;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class WarpListListener extends BaseGuiListener<WarpList> {

    private final NamespacedKey WARP_ID_KEY = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_RENDER_DATA_ID);

    public WarpListListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {}

    @Override
    protected @NotNull FunctionHandler<WarpList> registry() {
        FunctionHandler<WarpList> registry = new FunctionHandler<>();

        registry.add(FunctionType.BACK, (event, warpList, player) -> event.getInventory().close());
        registry.add(FunctionType.DATA, (event, warpList, player) -> {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            String warpId = currentItem.getItemMeta().getPersistentDataContainer().get(this.WARP_ID_KEY, PersistentDataType.STRING);
            //从数据库查询最新的
            Ari.REPOSITORY_MANAGER.get(ServerWarp.class).get(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpId), PartitionKey.global()).thenAccept((instance) -> {
                if (instance == null) {
                    Ari.instance.getLog().error("can't find warpId: {}", warpId);
                    return;
                }
                boolean isOwner = UUID.fromString(instance.getCreateBy()).equals(player.getUniqueId());
                if (event.isLeftClick()) {
                    Location targetLocation = FormatUtils.parseLocation(instance.getLocation());
                    Ari.STATE_MACHINE_MANAGER
                            .get(TeleportStateService.class)
                            .addState(new EntityToLocationCallbackState(
                                    player,
                                    Ari.instance.getConfigInstance().getValue("main.teleport.delay", FilePath.WARP_CONFIG, Integer.class, 3),
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
                                                Ari.instance.getConfigInstance().getValue("main.permission", FilePath.WARP_CONFIG, Boolean.class, true)) {
                                            ConfigUtils.t("function.warp.not-enough-money", player).thenAccept(player::sendMessage);
                                            return false;
                                        }
                                        return true;
                                    },
                                    () -> {
                                        //判断是否是地标拥有者或者是不是op，如果是则不扣
                                        if(!isOwner &&
                                                !player.isOp() &&
                                                Ari.instance.getConfigInstance().getValue("main.cost", FilePath.WARP_CONFIG, Boolean.class, false) &&
                                                !Ari.ECONOMY_SERVICE.isNull()) {
                                            Ari.ECONOMY_SERVICE.withdrawPlayer(player, instance.getCost());
                                            player.sendMessage(ConfigUtils.tAfter("teleport.costed", Map.of(LangVault.COSTED_UNRESOLVED.getType(), Component.text(instance.getCost().toString() + Ari.ECONOMY_SERVICE.getNamePlural()))));
                                        }
                                    },
                                    TeleportType.WARP));
                    Ari.instance.getScheduler().runAtEntity(Ari.instance, player, i -> event.getInventory().close(), null);
                } else if (event.isRightClick()) {
                    if(isOwner || player.isOp()) {
                        Ari.instance.getScheduler().run(Ari.instance, i -> {
                            event.getInventory().close();
                            player.openInventory(new WarpEditor(instance, player).getInventory());
                        });
                    } else {
                        ConfigUtils.t("function.warp.no-permission-edit", player).thenAccept(player::sendMessage);
                    }
                }
            }).exceptionally(i -> {
                Ari.instance.getLog().error(i, "get warp id {} error", warpId);
                return null;
            });
        });
        registry.add(FunctionType.PREV_PAGE, (event, warpList, player) -> warpList.prev());
        registry.add(FunctionType.NEXT_PAGE, (event, warpList, player) -> warpList.next());

        return registry;
    }

    @Override
    protected boolean whenClick(InventoryClickEvent event, WarpList holder) {
        return false;
    }

    @Override
    protected boolean whenDoubleClick(InventoryClickEvent event, WarpList holder) {
        return false;
    }

    @Override
    protected boolean whenShiftClick(InventoryClickEvent event, WarpList holder) {
        return false;
    }

    @Override
    protected boolean whenDrag(InventoryDragEvent event, WarpList holder) {
        return false;
    }

}
