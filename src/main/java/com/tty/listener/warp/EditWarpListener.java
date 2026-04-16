package com.tty.listener.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.annotations.function_type.FunctionHandlerRegistry;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.state.EditGuiState;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.gui.warp.WarpEditor;
import com.tty.gui.warp.WarpList;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.listener.OnGuiEditListener;
import com.tty.states.GuiEditStateService;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EditWarpListener extends OnGuiEditListener<WarpEditor> {

    public EditWarpListener(GuiType guiType) {
        super(Ari.instance, new FunctionHandlerRegistry(new FunctionRegistry()), guiType);
    }

    @Override
    public boolean onTitleEditStatus(String message, EditGuiState state) {
        FunctionType type = state.getFunctionType();
        Player player = (Player) state.getOwner();
        List<String> value = Ari.C_INSTANCE.getValue("main.name-check", FilePath.WARP_CONFIG, new TypeToken<List<String>>(){}.getType(), List.of());
        if(value == null) {
            Ari.LOG.error("name-check list is null, check config");
            player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
            return false;
        }
        WarpEditor warpEditor = (WarpEditor) state.getI();
        switch (type) {
            case RENAME -> {
                if(!FormatUtils.checkName(message) || value.contains(message) || !FormatUtils.checkName(message)) {
                    player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-error")));
                    return false;
                }
                if(message.length() > Ari.C_INSTANCE.getValue("main.name-length", FilePath.WARP_CONFIG, new TypeToken<Integer>(){}.getType(), 15)) {
                    player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
                    return false;
                }
                warpEditor.getCurrentEditWarp().setWarpName(message);
            }
            case PERMISSION -> {
                if(!FormatUtils.isValidPermissionNode(message)) {
                    player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.permission.permission-error")));
                    return false;
                }
                warpEditor.getCurrentEditWarp().setPermission(message);
            }
            case COST -> {
                try {
                    Double i = Double.parseDouble(message);
                    warpEditor.getCurrentEditWarp().setCost(i);
                } catch (NumberFormatException e) {
                    player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.cost.format-error")));
                    return false;
                }
            }
        }
        Ari.SCHEDULER.runAtEntity(Ari.instance, player, i -> player.openInventory(warpEditor.getInventory()), () -> {});
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

    private static final class FunctionRegistry {

        @FunctionHandler(FunctionType.REBACK)
        public void onReback(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {
            event.getInventory().close();
            player.openInventory(new WarpList(player).getInventory());
        }

        @FunctionHandler(FunctionType.DELETE)
        public void onDelete(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {
            EntityRepository<ServerWarp> repository = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);

            LambdaQueryWrapper<ServerWarp> wrapper = new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, holder.getCurrentEditWarp().getWarpId());

            repository.delete(wrapper, PartitionKey.global()).thenCompose(i -> {
                if (i) {
                    return ConfigUtils.t("function.warp.delete-success", player).thenAccept(player::sendMessage)
                            .thenRun(() -> Ari.SCHEDULER.run(Ari.instance, ab -> {
                                event.getInventory().close();
                                player.openInventory(new WarpList(player).getInventory());
                            }));
                } else {
                    return ConfigUtils.t("function.warp.not-found").thenAccept(player::sendMessage);
                }
            }).exceptionally(i -> {
                Ari.LOG.error(i, "deleting warp error");
                return null;
            });
        }

        @FunctionHandler(FunctionType.RENAME)
        public void onRename(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {
            this.onPublic(type, event, holder, player);
        }

        @FunctionHandler(FunctionType.COST)
        public void onCost(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {
            this.onPublic(type, event, holder, player);
        }

        @FunctionHandler(FunctionType.PERMISSION)
        public void onPermission(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {
            this.onPublic(type, event, holder, player);
        }

        @FunctionHandler(FunctionType.LOCATION)
        public void onLocation(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            Location newLocation = player.getLocation();
            holder.getCurrentEditWarp().setLocation(newLocation.toString());
            clickMeta.displayName(ComponentUtils.text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
            clickItem.setItemMeta(clickMeta);
        }

        @FunctionHandler(FunctionType.ICON)
        public void onIcon(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            NamespacedKey icon_type = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_RENDER_FUNCTION_ICON);

            ItemStack cursor = event.getCursor();
            Material current = cursor.getType();
            if(current.equals(Material.AIR)) return;
            ItemStack newItemStake = ItemStack.of(current);
            ItemMeta newItemMeta = newItemStake.getItemMeta();
            newItemMeta.displayName(clickMeta.displayName());
            newItemMeta.lore(clickItem.lore());
            String string = clickMeta.getPersistentDataContainer().get(icon_type, PersistentDataType.STRING);
            if (string == null) return;
            newItemMeta.getPersistentDataContainer().set(icon_type, PersistentDataType.STRING, string);
            newItemStake.setItemMeta(newItemMeta);
            event.getInventory().setItem(event.getSlot(), newItemStake);
            holder.getCurrentEditWarp().setShowMaterial(current.name());
        }

        @FunctionHandler(FunctionType.SAVE)
        public void onSave(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            EntityRepository<ServerWarp> repository = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);

            LambdaQueryWrapper<ServerWarp> wrapper = new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, holder.getCurrentEditWarp().getWarpId());

            Ari.LOG.debug("start saving warp id: {}", holder.getCurrentEditWarp().getWarpId());
            clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.ing"))));
            clickItem.setItemMeta(clickMeta);
            CompletableFuture<Boolean> future = repository.update(holder.getCurrentEditWarp(), wrapper, PartitionKey.global());
            future.thenAccept(status -> {
                clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue(status ? "base.save.done":"base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                if(status) {
                    Ari.SCHEDULER.runAsyncDelayed(Ari.instance, e ->{
                        clickMeta.lore(List.of());
                        clickItem.setItemMeta(clickMeta);
                    }, 20L);
                } else {
                    clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                }
            }).exceptionally(i -> {
                Ari.LOG.error(i, "saving warp error");
                clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                return null;
            });
        }

        @FunctionHandler(FunctionType.TOP_SLOT)
        public void onTopSlot(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            ServerWarp currentEditWarp = holder.getCurrentEditWarp();

            currentEditWarp.setTopSlot(!currentEditWarp.isTopSlot());
            holder.getBaseMenu().getFunctionItems().forEach((k, v) -> {
                if (v.getType().equals(FunctionType.TOP_SLOT)) {
                    List<String> lore = v.getLore();
                    List<TextComponent> list = lore.stream().map(p -> ComponentUtils.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), ComponentUtils.text(Ari.DATA_SERVICE.getValue(currentEditWarp.isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                    clickMeta.lore(list);
                    clickItem.setItemMeta(clickMeta);
                }
            });
        }

        private void onPublic(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            ServerWarp currentEditWarp = holder.getCurrentEditWarp();

            //检查是否有经济插件，如果没有就return
            if (type.equals(FunctionType.COST) && Ari.ECONOMY_SERVICE.isNull()) return;
            if (type.equals(FunctionType.PERMISSION) && event.getClick().isRightClick()) {
                clickMeta.displayName(ComponentUtils.text(""));
                clickItem.setItemMeta(clickMeta);
                currentEditWarp.setPermission(null);
                return;
            }
            Ari.STATE_MACHINE_MANAGER.get(GuiEditStateService.class)
                    .addState(new EditGuiState(
                                    player,
                                    Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new com.google.common.reflect.TypeToken<Integer>(){}.getType()),
                                    new WarpEditor(PublicFunctionUtils.deepCopy(currentEditWarp, ServerWarp.class), player),
                                    type
                            )
                    );
            event.getInventory().close();
        }

    }

}
