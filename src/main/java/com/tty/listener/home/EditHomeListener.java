package com.tty.listener.home;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.state.PlayerEditGuiState;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.api.enumType.GuiType;
import com.tty.gui.home.HomeEditor;
import com.tty.gui.home.HomeList;
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

public class EditHomeListener extends OnGuiEditListener {

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
        FunctionType type = this.ItemNBT_TypeCheck(clickMeta.getPersistentDataContainer().get(icon_type, PersistentDataType.STRING));
        event.setCancelled(true);
        if (type == null) return;

        EntityRepository<ServerHome> repository = Ari.REPOSITORY_MANAGER.get(ServerHome.class);
        switch (type) {
            case REBACK -> {
                inventory.close();
                player.openInventory(new HomeList(player).getInventory());
            }
            case DELETE ->
                repository.delete(homeEditor.currentHome)
                    .thenCompose(success -> {
                        if (success) {
                            return ConfigUtils.t("function.home.delete-success", player)
                                    .thenAccept(player::sendMessage)
                                    .thenRun(() -> Ari.SCHEDULER.run(Ari.instance, j -> {
                                        inventory.close();
                                        player.openInventory(new HomeList(player).getInventory());
                                    }));
                        } else {
                            return ConfigUtils.t("function.home.not-found", player).thenAccept(player::sendMessage);
                        }
                    });
            case RENAME -> {
                Ari.STATE_MACHINE_MANAGER
                        .get(GuiEditStateService.class)
                        .addState(new PlayerEditGuiState(
                                player,
                                Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new com.google.common.reflect.TypeToken<Integer>(){}.getType()),
                                new HomeEditor(PublicFunctionUtils.deepCopy(homeEditor.currentHome, ServerHome.class), player),
                                type)
                        );
                inventory.close();
            }
            case LOCATION -> {
                //reset LOCATION
                Location newLocation = player.getLocation();
                homeEditor.currentHome.setLocation(newLocation.toString());
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
                homeEditor.currentHome.setShowMaterial(current.name());
            }
            case TOP_SLOT -> {
                homeEditor.currentHome.setTopSlot(!homeEditor.currentHome.isTopSlot());
                homeEditor.getBaseMenu().getFunctionItems().forEach((k, v) -> {
                    if (v.getType().equals(FunctionType.TOP_SLOT)) {
                        List<String> lore = v.getLore();
                        List<TextComponent> list = lore.stream().map(p -> Ari.COMPONENT_SERVICE.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue(homeEditor.currentHome.isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                        clickMeta.lore(list);
                        clickItem.setItemMeta(clickMeta);
                    }
                });
            }
            case SAVE -> {
                clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.save.ing"))));
                clickItem.setItemMeta(clickMeta);
                repository.update(homeEditor.currentHome).thenAccept(status -> {
                    clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue(status ? "base.save.done":"base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    Ari.SCHEDULER.runAsyncDelayed(Ari.instance, e -> {
                        clickMeta.lore(List.of());
                        clickItem.setItemMeta(clickMeta);
                    }, 20);
                }).exceptionally(i -> {
                    Log.error(i, "save home error");
                    clickMeta.lore(List.of(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                    clickItem.setItemMeta(clickMeta);
                    player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
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
            player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-error")));
            return false;
        }
        if(message.length() > Ari.C_INSTANCE.getValue("main.name-length", FilePath.HOME_CONFIG, Integer.class, 15)) {
            player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
            return false;
        }
        HomeEditor homeEditor = (HomeEditor) state.getI();
        homeEditor.currentHome.setHomeName(message);
        Ari.SCHEDULER.runAtEntity(Ari.instance, player, p -> player.openInventory(homeEditor.getInventory()), () -> {});
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

}
