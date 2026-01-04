package com.tty.listener.warp;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.lib.dto.state.PlayerEditGuiState;
import com.tty.entity.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.function.WarpManager;
import com.tty.gui.warp.WarpEditor;
import com.tty.gui.warp.WarpList;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.listener.OnGuiEditListener;
import com.tty.states.GuiEditStateService;
import com.tty.tool.ConfigUtils;
import com.tty.lib.tool.EconomyUtils;
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
        FunctionType type = FormatUtils.ItemNBT_TypeCheck(clickMeta.getPersistentDataContainer().get(icon_type, PersistentDataType.STRING));
        if(type == null) return;

        WarpManager warpManager = new WarpManager(true);
        switch (type) {
            case REBACK -> {
                inventory.close();
                new WarpList(player).open();
            }
            case DELETE -> warpManager.deleteInstance(warpEditor.currentWarp).thenAccept(i -> {
                if (i) {
                    player.sendMessage(ConfigUtils.t("function.warp.delete-success"));
                    Lib.Scheduler.run(Ari.instance, ab -> {
                        inventory.close();
                        new WarpList(player).open();
                    });
                } else {
                    player.sendMessage(ConfigUtils.t("function.warp.not-found"));
                }
            }).exceptionally(i -> {
                Log.error(i, "deleting warp error");
                return null;
            });
            case RENAME, COST, PERMISSION -> {
                //检查是否有经济插件，如果没有就return
                if (type.equals(FunctionType.COST) && EconomyUtils.isNull()) return;
                if (type.equals(FunctionType.PERMISSION) && event.getClick().isRightClick()) {
                    clickMeta.displayName(ComponentUtils.text(""));
                    clickItem.setItemMeta(clickMeta);
                    warpEditor.currentWarp.setPermission(null);
                    return;
                }
                Ari.instance.stateMachineManager.get(GuiEditStateService.class)
                        .addState(new PlayerEditGuiState(
                                        player,
                                        new WarpEditor(PublicFunctionUtils.deepCopy(warpEditor.currentWarp, ServerWarp.class), player),
                                        type
                                )
                        );
                inventory.close();
            }
            case LOCATION -> {
                Location newLocation = player.getLocation();
                warpEditor.currentWarp.setLocation(newLocation.toString());
                clickMeta.displayName(ComponentUtils.text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
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
                Log.debug("start saving warp id: %s", warpEditor.currentWarp.getWarpId());
                clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue("base.save.ing"))));
                clickItem.setItemMeta(clickMeta);
                CompletableFuture<Boolean> future = warpManager.modify(warpEditor.currentWarp);
                future.thenAccept(status -> {
                    clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue(status ? "base.save.done":"base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    if(status) {
                        Lib.Scheduler.runAsyncDelayed(Ari.instance, e ->{
                            clickMeta.lore(List.of());
                            clickItem.setItemMeta(clickMeta);
                        }, 20L);
                    } else {
                        clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue("base.save.error"))));
                        clickItem.setItemMeta(clickMeta);
                    }
                }).exceptionally(i -> {
                    Log.error(i, "saving warp error");
                    clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue("base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    return null;
                });
            }
            case TOP_SLOT -> {
                warpEditor.currentWarp.setTopSlot(!warpEditor.currentWarp.isTopSlot());
                warpEditor.baseInstance.getFunctionItems().forEach((k, v) -> {
                    if (v.getType().equals(FunctionType.TOP_SLOT)) {
                        List<String> lore = v.getLore();
                        List<TextComponent> list = lore.stream().map(p -> ComponentUtils.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), ComponentUtils.text(Ari.instance.dataService.getValue(warpEditor.currentWarp.isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
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
            player.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
            return false;
        }
        WarpEditor warpEditor = (WarpEditor) state.getI();
        switch (type) {
            case RENAME -> {
                if(!FormatUtils.checkName(message) || value.contains(message) || !FormatUtils.checkName(message)) {
                    player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.rename.name-error")));
                    return false;
                }
                if(message.length() > Ari.C_INSTANCE.getValue("main.name-length", FilePath.WARP_CONFIG, new TypeToken<Integer>(){}.getType(), 15)) {
                    player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.rename.name-too-long")));
                    return false;
                }
                warpEditor.currentWarp.setWarpName(message);
            }
            case PERMISSION -> {
                if(!FormatUtils.isValidPermissionNode(message)) {
                    player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.permission.permission-error")));
                    return false;
                }
                warpEditor.currentWarp.setPermission(message);
            }
            case COST -> {
                try {
                    Double i = Double.parseDouble(message);
                    warpEditor.currentWarp.setCost(i);
                } catch (NumberFormatException e) {
                    player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.cost.format-error")));
                    return false;
                }
            }
        }
        Lib.Scheduler.runAtEntity(Ari.instance, player, i -> warpEditor.open(), () -> {});
        return true;
    }

}
