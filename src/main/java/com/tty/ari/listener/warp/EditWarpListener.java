package com.tty.ari.listener.warp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.api.state.GuiEditFunctionState;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.warp.WarpConfig;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.entity.ServerWarp;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.gui.warp.WarpEditor;
import com.tty.ari.gui.warp.WarpList;
import com.tty.ari.listener.OnGuiEditListener;
import com.tty.ari.states.gui.GuiEditFunctionStateService;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EditWarpListener extends OnGuiEditListener<WarpEditor, ServerWarp> {

    public EditWarpListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    public boolean onTitleEditStatus(String message, GuiEditFunctionState<ServerWarp> state) {
        FunctionType type = state.getFunctionType();
        Player player = (Player) state.getOwner();
        WarpConfig warpConfig = Ari.instance.getConfigurationManager().get(WarpConfig.class);

        ServerWarp data = state.getData();
        switch (type) {
            case RENAME -> {
                if(!this.isContentValid(message) || warpConfig.checkWarpNickName().contains(message)) {
                    player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-error")));
                    return false;
                }
                if(message.length() > warpConfig.getWarpNameLength()) {
                    player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
                    return false;
                }
                data.setWarpName(message);
            }
            case PERMISSION -> {
                if(!this.isValidPermissionNode(message)) {
                    player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.permission.permission-error")));
                    return false;
                }
                data.setPermission(message);
            }
            case COST -> {
                try {
                    Double i = Double.parseDouble(message);
                    data.setCost(i);
                } catch (NumberFormatException e) {
                    player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.cost.format-error")));
                    return false;
                }
            }
        }
        Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new GuiState(player, new WarpEditor(player, data)));
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

    @Override
    protected @NotNull FunctionHandler<WarpEditor> registry() {
        FunctionHandler<WarpEditor> registry = new FunctionHandler<>();

        registry.add(FunctionType.REBACK, (event, warpEditor, player) -> {
            event.getInventory().close();
            Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new GuiState(player, new WarpList(player)));
        });
        registry.add(FunctionType.DELETE, (event, warpEditor, player) -> {
            EntityRepository<ServerWarp> repository = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);
            LambdaQueryWrapper<ServerWarp> wrapper = new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpEditor.getWarp().getWarpId());
            repository.delete(wrapper, PartitionKey.global()).thenCompose(i -> {
                if (i) {
                    return ConfigUtils.t("function.warp.delete-success", player).thenAccept(player::sendMessage)
                            .thenRun(() -> Ari.instance.getScheduler().run(Ari.instance, ab -> {
                                event.getInventory().close();
                                Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new GuiState(player, new WarpList(player)));
                            }));
                } else {
                    return ConfigUtils.t("function.warp.not-found").thenAccept(player::sendMessage);
                }
            }).exceptionally(i -> {
                Ari.instance.getLog().error(i, "deleting warp error");
                return null;
            });
        });
        registry.add(FunctionType.RENAME, (event, warpEditor, player) -> this.onPublic(FunctionType.RENAME, event, warpEditor, player));
        registry.add(FunctionType.COST, (event, warpEditor, player) -> this.onPublic(FunctionType.COST, event, warpEditor, player));
        registry.add(FunctionType.PERMISSION, (event, warpEditor, player) -> this.onPublic(FunctionType.PERMISSION, event, warpEditor, player));
        registry.add(FunctionType.LOCATION, (event, warpEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            Location newLocation = player.getLocation();
            warpEditor.getWarp().setLocation(newLocation.toString());
            clickMeta.displayName(Ari.instance.getComponentTool().text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
            clickItem.setItemMeta(clickMeta);
        });
        registry.add(FunctionType.ICON, (event, warpEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            ItemStack cursor = event.getCursor();
            Material current = cursor.getType();
            if(current.equals(Material.AIR)) return;
            ItemStack newItemStake = ItemStack.of(current);
            ItemMeta newItemMeta = newItemStake.getItemMeta();
            newItemMeta.displayName(clickMeta.displayName());
            newItemMeta.lore(clickItem.lore());

            String string = Ari.instance.getNbtManager().getNbt(NbtGuiValue.GUI_FUNCTION_ICON, clickItem, PersistentDataType.STRING);
            if (string == null) return;
            Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, newItemStake, PersistentDataType.STRING, string);
            newItemStake.setItemMeta(newItemMeta);
            event.getInventory().setItem(event.getSlot(), newItemStake);
            warpEditor.getWarp().setShowMaterial(current.name());
        });
        registry.add(FunctionType.SAVE, (event, warpEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            EntityRepository<ServerWarp> repository = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);

            LambdaQueryWrapper<ServerWarp> wrapper = new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpEditor.getWarp().getWarpId());

            Ari.instance.getLog().debug("start saving warp id: {}", warpEditor.getWarp().getWarpId());
            clickMeta.lore(List.of(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.save.ing"))));
            clickItem.setItemMeta(clickMeta);
            CompletableFuture<Boolean> future = repository.update(warpEditor.getWarp(), wrapper, PartitionKey.global());
            future.thenAccept(status -> {
                clickMeta.lore(List.of(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue(status ? "base.save.done":"base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                if(status) {
                    Ari.instance.getScheduler().runAsyncDelayed(Ari.instance, e ->{
                        clickMeta.lore(List.of());
                        clickItem.setItemMeta(clickMeta);
                    }, 20L);
                } else {
                    clickMeta.lore(List.of(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                }
            }).exceptionally(i -> {
                Ari.instance.getLog().error(i, "saving warp error");
                clickMeta.lore(List.of(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                return null;
            });
        });
        registry.add(FunctionType.TOP_SLOT, (event, warpEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();
            ServerWarp currentEditWarp = warpEditor.getWarp();
            currentEditWarp.setTopSlot(!currentEditWarp.isTopSlot());
            warpEditor.getBaseMenu().getFunctionItems().forEach((k, v) -> {
                if (v.getType().equals(FunctionType.TOP_SLOT)) {
                    List<String> lore = v.getLore();
                    List<TextComponent> list = lore.stream().map(p -> Ari.instance.getComponentTool().text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue(currentEditWarp.isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                    clickMeta.lore(list);
                    clickItem.setItemMeta(clickMeta);
                }
            });
        });
        return registry;
    }

    @Override
    protected void whenClick(InventoryClickEvent event, WarpEditor holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, WarpEditor holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, WarpEditor holder) {
        event.setCancelled(true);
    }

    private void onPublic(FunctionType type, InventoryClickEvent event, WarpEditor holder, Player player) {

        ItemStack clickItem = event.getCurrentItem();
        if (clickItem == null) return;
        ItemMeta clickMeta = clickItem.getItemMeta();

        ServerWarp currentEditWarp = holder.getWarp();

        //检查是否有经济插件，如果没有就return
        if (type.equals(FunctionType.COST) && Ari.ECONOMY_SERVICE.isNull()) return;
        if (type.equals(FunctionType.PERMISSION) && event.getClick().isRightClick()) {
            clickMeta.displayName(Ari.instance.getComponentTool().text(""));
            clickItem.setItemMeta(clickMeta);
            currentEditWarp.setPermission(null);
            return;
        }
        event.getInventory().close();
        Ari.STATE_MACHINE_MANAGER.get(GuiEditFunctionStateService.class).addState(
                new GuiEditFunctionState<>(
                        player,
                        Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new TypeToken<Integer>() {}.getType()),
                        currentEditWarp,
                        type,
                        GuiType.WARP_EDIT
                )
        );
    }

}
