package com.tty.listener.warp;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.state.PlayerEditGuiState;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.gui.warp.WarpEditor;
import com.tty.gui.warp.WarpList;
import com.tty.api.Log;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.repository.EntityRepository;
import com.tty.api.FormatUtils;
import com.tty.api.PublicFunctionUtils;
import com.tty.listener.OnGuiEditListener;
import com.tty.states.GuiEditStateService;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EditWarpListener extends OnGuiEditListener {

    public EditWarpListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {
        super.passClick(event);
        Inventory inventory = event.getInventory();
        ItemStack clickItem = event.getCurrentItem();
        assert clickItem != null;
        WarpEditor warpEditor = (WarpEditor) inventory.getHolder();
        assert warpEditor != null;
        Player player = (Player) event.getWhoClicked();

        ItemMeta clickMeta = clickItem.getItemMeta();
        NamespacedKey icon_type = new NamespacedKey(Ari.instance, "type");
        FunctionType type = this.ItemNBT_TypeCheck(clickMeta.getPersistentDataContainer().get(icon_type, PersistentDataType.STRING));
        if(type == null) return;
        EntityRepository<ServerWarp> warpEntityRepository = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);
        switch (type) {
            case REBACK -> {
                inventory.close();
                player.openInventory(new WarpList(player).getInventory());
            }
            case DELETE -> warpEntityRepository.delete(warpEditor.currentWarp).thenCompose(i -> {
                if (i) {
                    return ConfigUtils.t("function.warp.delete-success", player).thenAccept(player::sendMessage)
                            .thenRun(() -> Ari.SCHEDULER.run(Ari.instance, ab -> {
                                inventory.close();
                                player.openInventory(new WarpList(player).getInventory());
                            }));
                } else {
                    return ConfigUtils.t("function.warp.not-found").thenAccept(player::sendMessage);
                }
            }).exceptionally(i -> {
                Log.error(i, "deleting warp error");
                return null;
            });
            case RENAME, COST, PERMISSION -> {
                //检查是否有经济插件，如果没有就return
                if (type.equals(FunctionType.COST) && Ari.ECONOMY_SERVICE.isNull()) return;
                if (type.equals(FunctionType.PERMISSION) && event.getClick().isRightClick()) {
                    clickMeta.displayName(Ari.COMPONENT_SERVICE.text(""));
                    clickItem.setItemMeta(clickMeta);
                    warpEditor.currentWarp.setPermission(null);
                    return;
                }
                Ari.STATE_MACHINE_MANAGER.get(GuiEditStateService.class)
                        .addState(new PlayerEditGuiState(
                                        player,
                                Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new com.google.common.reflect.TypeToken<Integer>(){}.getType()),
                                        new WarpEditor(PublicFunctionUtils.deepCopy(warpEditor.currentWarp, ServerWarp.class), player),
                                        type
                                )
                        );
                inventory.close();
            }
            case LOCATION -> {
                Location newLocation = player.getLocation();
                warpEditor.currentWarp.setLocation(newLocation.toString());
                clickMeta.displayName(Ari.COMPONENT_SERVICE.text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
                clickItem.setItemMeta(clickMeta);
            }
            case ICON -> {
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
                inventory.setItem(event.getSlot(), newItemStake);
                warpEditor.currentWarp.setShowMaterial(current.name());
            }
            case SAVE -> {
                Log.debug("start saving warp id: {}", warpEditor.currentWarp.getWarpId());
                clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.save.ing"))));
                clickItem.setItemMeta(clickMeta);
                CompletableFuture<Boolean> future = warpEntityRepository.update(warpEditor.currentWarp);
                future.thenAccept(status -> {
                    clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue(status ? "base.save.done":"base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    if(status) {
                        Ari.SCHEDULER.runAsyncDelayed(Ari.instance, e ->{
                            clickMeta.lore(List.of());
                            clickItem.setItemMeta(clickMeta);
                        }, 20L);
                    } else {
                        clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                        clickItem.setItemMeta(clickMeta);
                    }
                }).exceptionally(i -> {
                    Log.error(i, "saving warp error");
                    clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    return null;
                });
            }
            case TOP_SLOT -> {
                warpEditor.currentWarp.setTopSlot(!warpEditor.currentWarp.isTopSlot());
                warpEditor.getBaseMenu().getFunctionItems().forEach((k, v) -> {
                    if (v.getType().equals(FunctionType.TOP_SLOT)) {
                        List<String> lore = v.getLore();
                        List<TextComponent> list = lore.stream().map(p -> Ari.COMPONENT_SERVICE.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue(warpEditor.currentWarp.isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                        clickMeta.lore(list);
                        clickItem.setItemMeta(clickMeta);
                    }
                });
            }
        }
    }

    @Override
    public boolean onTitleEditStatus(String message, PlayerEditGuiState state) {
        FunctionType type = state.getFunctionType();
        Player player = (Player) state.getOwner();
        List<String> value = Ari.C_INSTANCE.getValue("main.name-check", FilePath.WARP_CONFIG, new TypeToken<List<String>>(){}.getType(), List.of());
        if(value == null) {
            Log.error("name-check list is null, check config");
            player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
            return false;
        }
        WarpEditor warpEditor = (WarpEditor) state.getI();
        switch (type) {
            case RENAME -> {
                if(!FormatUtils.checkName(message) || value.contains(message) || !FormatUtils.checkName(message)) {
                    player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-error")));
                    return false;
                }
                if(message.length() > Ari.C_INSTANCE.getValue("main.name-length", FilePath.WARP_CONFIG, new TypeToken<Integer>(){}.getType(), 15)) {
                    player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
                    return false;
                }
                warpEditor.currentWarp.setWarpName(message);
            }
            case PERMISSION -> {
                if(!FormatUtils.isValidPermissionNode(message)) {
                    player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.permission.permission-error")));
                    return false;
                }
                warpEditor.currentWarp.setPermission(message);
            }
            case COST -> {
                try {
                    Double i = Double.parseDouble(message);
                    warpEditor.currentWarp.setCost(i);
                } catch (NumberFormatException e) {
                    player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.cost.format-error")));
                    return false;
                }
            }
        }
        Ari.SCHEDULER.runAtEntity(Ari.instance, player, i -> player.openInventory(warpEditor.getInventory()), () -> {});
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

}
