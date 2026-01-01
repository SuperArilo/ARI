package com.tty.listener.home;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.dto.state.player.PlayerEditGuiState;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.function.HomeManager;
import com.tty.gui.home.HomeEditor;
import com.tty.gui.home.HomeList;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.listener.BaseEditFunctionGuiListener;
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

public class EditHomeListener extends BaseEditFunctionGuiListener {

    public EditHomeListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {
        super.passClick(event);
        Inventory inventory = event.getInventory();
        HomeEditor homeEditor = (HomeEditor) inventory.getHolder();
        assert homeEditor != null;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickItem = event.getCurrentItem();
        assert clickItem != null;

        ItemMeta clickMeta = clickItem.getItemMeta();
        NamespacedKey icon_type = new NamespacedKey(Ari.instance, "type");
        FunctionType type = FormatUtils.ItemNBT_TypeCheck(clickMeta.getPersistentDataContainer().get(icon_type, PersistentDataType.STRING));
        event.setCancelled(true);
        if (type == null) return;

        HomeManager homeManager = new HomeManager(player, true);
        switch (type) {
            case REBACK -> {
                inventory.close();
                new HomeList(player).open();
            }
            case DELETE -> //delete home
                    homeManager.deleteInstance(homeEditor.currentHome).thenAccept(i -> {
                        if (i) {
                            player.sendMessage(ConfigUtils.t("function.home.delete-success"));
                            Lib.Scheduler.run(Ari.instance, j -> {
                                inventory.close();
                                new HomeList(player).open();
                            });
                        } else {
                            player.sendMessage(ConfigUtils.t("function.home.not-found"));
                        }

                    });
            case RENAME -> {
                Ari.instance.stateMachineManager
                        .get(GuiEditStateService.class)
                        .addState(new PlayerEditGuiState(
                                player,
                                new HomeEditor(PublicFunctionUtils.deepCopy(homeEditor.currentHome, ServerHome.class), player),
                                type)
                        );
                inventory.close();
            }
            case LOCATION -> {
                //reset LOCATION
                Location newLocation = player.getLocation();
                homeEditor.currentHome.setLocation(newLocation.toString());
                clickMeta.displayName(ComponentUtils.text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
                clickItem.setItemMeta(clickMeta);
            }
            case ICON -> {
                ItemStack cursor = event.getCursor();
                Material current = cursor.getType();
                if(current.equals(Material.AIR)) return;
                ItemStack newItemStake = new ItemStack(current);
                ItemMeta newItemMeta = newItemStake.getItemMeta();
                newItemMeta.displayName(clickMeta.displayName());
                newItemMeta.lore(clickItem.lore());
                String string = clickMeta.getPersistentDataContainer().get(icon_type, PersistentDataType.STRING);
                if (string == null) return;
                newItemMeta.getPersistentDataContainer().set(icon_type, PersistentDataType.STRING, string);
                newItemStake.setItemMeta(newItemMeta);
                inventory.setItem(event.getSlot(), newItemStake);
                homeEditor.currentHome.setShowMaterial(current.name());
            }
            case TOP_SLOT -> {
                homeEditor.currentHome.setTopSlot(!homeEditor.currentHome.isTopSlot());
                homeEditor.baseInstance.getFunctionItems().forEach((k, v) -> {
                    if (v.getType().equals(FunctionType.TOP_SLOT)) {
                        List<String> lore = v.getLore();
                        List<TextComponent> list = lore.stream().map(p -> ComponentUtils.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), ComponentUtils.text(Ari.instance.dataService.getValue(homeEditor.currentHome.isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                        clickMeta.lore(list);
                        clickItem.setItemMeta(clickMeta);
                    }
                });
            }
            case SAVE -> {
                clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue("base.save.ing"))));
                clickItem.setItemMeta(clickMeta);
                CompletableFuture<Boolean> future = homeManager.modify(homeEditor.currentHome);
                future.thenAccept(status -> {
                    clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue(status ? "base.save.done":"base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    Lib.Scheduler.runAsyncDelayed(Ari.instance, e -> {
                        clickMeta.lore(List.of());
                        clickItem.setItemMeta(clickMeta);
                    }, 20);
                }).exceptionally(i -> {
                    Log.error(i, "save home error");
                    clickMeta.lore(List.of(ComponentUtils.text(Ari.instance.dataService.getValue("base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    player.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
                    return null;
                });
            }
        }
    }

    @Override
    public boolean onTitleEditStatus(String message, PlayerEditGuiState state) {
        Player player = (Player) state.getOwner();
        List<Object> checkList = Ari.C_INSTANCE
                .getValue(
                        "main.name-check",
                        FilePath.HOME_CONFIG,
                        new TypeToken<List<String>>() {
                        }.getType(),
                        List.of());
        if(!FormatUtils.checkName(message) || checkList.contains(message)) {
            player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.rename.name-error")));
            return false;
        }
        if(message.length() > Ari.C_INSTANCE.getValue("main.name-length", FilePath.HOME_CONFIG, Integer.class, 15)) {
            player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.rename.name-too-long")));
            return false;
        }
        HomeEditor homeEditor = (HomeEditor) state.getI();
        homeEditor.currentHome.setHomeName(message);
        Lib.Scheduler.runAtEntity(Ari.instance, player, p -> homeEditor.open(), () -> {});
        return true;
    }

}
