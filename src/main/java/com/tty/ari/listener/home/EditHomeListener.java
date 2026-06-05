package com.tty.ari.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.tty.ari.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.api.state.EditGuiState;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.gui.home.HomeEditor;
import com.tty.ari.gui.home.HomeList;
import com.tty.ari.listener.OnGuiEditListener;
import com.tty.ari.states.GuiEditStateService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class EditHomeListener extends OnGuiEditListener<HomeEditor> {

    public EditHomeListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    public boolean onTitleEditStatus(String message, EditGuiState state) {
        Player player = (Player) state.getOwner();
        List<Object> checkList = Ari.instance.getConfigInstance()
                .getValue(
                        "main.name-check",
                        FilePath.HOME_CONFIG,
                        new TypeToken<List<String>>() {
                        }.getType(),
                        List.of());
        if(!FormatUtils.checkName(message) || checkList.contains(message)) {
            player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-error")));
            return false;
        }
        if(message.length() > Ari.instance.getConfigInstance().getValue("main.name-length", FilePath.HOME_CONFIG, Integer.class, 15)) {
            player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
            return false;
        }
        HomeEditor homeEditor = (HomeEditor) state.getI();
        homeEditor.getCurrentEditHome().setHomeName(message);
        Ari.instance.getScheduler().runAtEntity(Ari.instance, player, p -> player.openInventory(homeEditor.getInventory()), () -> {});
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

    @Override
    protected @NotNull FunctionHandler<HomeEditor> registry() {
        FunctionHandler<HomeEditor> registry = new FunctionHandler<>();

        registry.add(FunctionType.REBACK, (event, homeEditor, player) -> {
            event.getInventory().close();
            player.openInventory(new HomeList(player).getInventory());
        });
        registry.add(FunctionType.DELETE, (event, homeEditor, player) -> {
            LambdaQueryWrapper<ServerHome> wrapper = new LambdaQueryWrapper<>(ServerHome.class)
                    .eq(ServerHome::getHomeId, homeEditor.getCurrentEditHome().getHomeId())
                    .eq(ServerHome::getPlayerUUID, homeEditor.getCurrentEditHome().getPlayerUUID());

            EntityRepository<ServerHome> repository = Ari.REPOSITORY_MANAGER.get(ServerHome.class);
            repository.delete(wrapper, PartitionKey.of(player.getUniqueId().toString()))
                    .thenCompose(success -> {
                        if (success) {
                            return ConfigUtils.t("function.home.delete-success", player)
                                    .thenAccept(player::sendMessage)
                                    .thenRun(() -> Ari.instance.getScheduler().run(Ari.instance, j -> {
                                        event.getInventory().close();
                                        player.openInventory(new HomeList(player).getInventory());
                                    }));
                        } else {
                            return ConfigUtils.t("function.home.not-found", player).thenAccept(player::sendMessage);
                        }
                    });
        });
        registry.add(FunctionType.RENAME, (event, homeEditor, player) -> {
            Ari.STATE_MACHINE_MANAGER
                    .get(GuiEditStateService.class)
                    .addState(new EditGuiState(
                            player,
                            Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new TypeToken<Integer>(){}.getType()),
                            new HomeEditor(PublicFunctionUtils.deepCopy(homeEditor.getCurrentEditHome(), ServerHome.class), player),
                            FunctionType.RENAME)
                    );
            event.getInventory().close();
        });
        registry.add(FunctionType.LOCATION, ((event, homeEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            Location newLocation = player.getLocation();
            homeEditor.getCurrentEditHome().setLocation(newLocation.toString());
            clickMeta.displayName(ComponentUtils.text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
            clickItem.setItemMeta(clickMeta);
        }));
        registry.add(FunctionType.ICON, (event, homeEditor, player) -> {
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

            NamespacedKey namespacedKey = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_RENDER_FUNCTION_ICON);
            String string = clickMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
            if (string == null) return;
            newItemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, string);
            newItemStake.setItemMeta(newItemMeta);
            event.getInventory().setItem(event.getSlot(), newItemStake);
            homeEditor.getCurrentEditHome().setShowMaterial(current.name());
        });
        registry.add(FunctionType.TOP_SLOT, (event, homeEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            homeEditor.getCurrentEditHome().setTopSlot(!homeEditor.getCurrentEditHome().isTopSlot());
            homeEditor.getBaseMenu().getFunctionItems().forEach((k, v) -> {
                if (v.getType().equals(FunctionType.TOP_SLOT)) {
                    List<String> lore = v.getLore();
                    List<TextComponent> list = lore.stream().map(p -> ComponentUtils.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), ComponentUtils.text(Ari.DATA_SERVICE.getValue(homeEditor.getCurrentEditHome().isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                    clickMeta.lore(list);
                    clickItem.setItemMeta(clickMeta);
                }
            });
        });
        registry.add(FunctionType.SAVE, (event, homeEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.ing"))));
            clickItem.setItemMeta(clickMeta);

            LambdaQueryWrapper<ServerHome> wrapper = new LambdaQueryWrapper<>(ServerHome.class)
                    .eq(ServerHome::getHomeId, homeEditor.getCurrentEditHome().getHomeId())
                    .eq(ServerHome::getPlayerUUID, homeEditor.getCurrentEditHome().getPlayerUUID());

            EntityRepository<ServerHome> repository = Ari.REPOSITORY_MANAGER.get(ServerHome.class);

            repository.update(homeEditor.getCurrentEditHome(), wrapper, PartitionKey.of(player.getUniqueId().toString())).thenAccept(status -> {
                clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue(status ? "base.save.done":"base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                Ari.instance.getScheduler().runAsyncDelayed(Ari.instance, e -> {
                    clickMeta.lore(List.of());
                    clickItem.setItemMeta(clickMeta);
                }, 20);
            }).exceptionally(i -> {
                Ari.instance.getLog().error(i, "save home error");
                clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
                return null;
            });
        });

        return registry;
    }

    @Override
    protected void whenClick(InventoryClickEvent event, HomeEditor holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, HomeEditor holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, HomeEditor holder) {
        event.setCancelled(true);
    }

}
