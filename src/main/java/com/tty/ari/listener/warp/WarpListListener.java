package com.tty.ari.listener.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.warp.WarpConfig;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.teleport.EntityToLocationCallbackState;
import com.tty.ari.entity.ServerWarp;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.enumType.lang.PlaceholderVault;
import com.tty.ari.gui.warp.WarpEditor;
import com.tty.ari.gui.warp.WarpList;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpListListener extends BaseGuiListener<WarpList> {

    public WarpListListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    protected @NotNull FunctionHandler<WarpList> registry() {
        FunctionHandler<WarpList> registry = new FunctionHandler<>();

        registry.addSync(FunctionType.BACK, (event, warpList, player) -> event.getInventory().close());
        registry.addAsync(FunctionType.DATA, (event, warpList, player) -> {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return CompletableFuture.completedFuture(null);

            String warpId = Ari.instance.getNbtManager().getNbt(NbtGuiValue.GUI_DATA_ID, currentItem, PersistentDataType.STRING);

            //从数据库查询最新的
            return Ari.REPOSITORY_MANAGER.get(ServerWarp.class).get(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpId), PartitionKey.global()).thenAccept((instance) -> {
                if (instance == null) {
                    Ari.instance.getLog().error("can't find warpId: {}", warpId);
                    ConfigUtils.t("function.warp.not-found", player).thenAccept(player::sendMessage);
                    return;
                }
                boolean isOwner = UUID.fromString(instance.getCreateBy()).equals(player.getUniqueId());
                if (event.isLeftClick()) {
                    Location targetLocation = FormatUtils.parseLocation(instance.getLocation());
                    WarpConfig warpConfig = Ari.instance.getConfigurationManager().get(WarpConfig.class);
                    Ari.instance.getStatusManager().get(TeleportStateService.class).addState(new EntityToLocationCallbackState(
                            player,
                            warpConfig.getDelay(),
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
                                if(!Ari.ECONOMY_SERVICE.hasEnoughBalance(player, instance.getCost()) && !isOwner && warpConfig.permission()) {
                                    ConfigUtils.t("function.warp.not-enough-money", player).thenAccept(player::sendMessage);
                                    return false;
                                }
                                return true;
                            },
                            () -> {
                                //判断是否是地标拥有者或者是不是op，如果是则不扣
                                if(!isOwner && !player.isOp() &&
                                        warpConfig.cost() &&
                                        !Ari.ECONOMY_SERVICE.isNull()) {
                                    Ari.ECONOMY_SERVICE.withdrawPlayer(player, instance.getCost());
                                    player.sendMessage(ConfigUtils.tAfter("teleport.costed", Map.of(PlaceholderVault.COSTED_UNRESOLVED.getType(), Component.text(instance.getCost().toString() + Ari.ECONOMY_SERVICE.getNamePlural()))));
                                }}, TeleportType.WARP));
                } else if (event.isRightClick()) {
                    if(isOwner || player.isOp()) {
                        Ari.instance.getStatusManager().get(GuiManagerStateService.class).addState(new GuiState(player, new WarpEditor(player, instance)));
                    } else {
                        ConfigUtils.t("function.warp.no-permission-edit", player).thenAccept(player::sendMessage);
                    }
                }
            }).whenComplete((s, ex) -> {
                if (ex != null) {
                    Ari.instance.getLog().error(ex, "get warp id {} error", warpId);
                }
                Ari.instance.getScheduler().runAtEntity(player, i -> event.getInventory().close(), null);
            });
        });
        registry.addSync(FunctionType.PREV_PAGE, (event, warpList, player) -> warpList.prev());
        registry.addSync(FunctionType.NEXT_PAGE, (event, warpList, player) -> warpList.next());

        return registry;
    }

    @Override
    protected void whenClick(InventoryClickEvent event, WarpList holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, WarpList holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, WarpList holder) {
        event.setCancelled(true);
    }

}
