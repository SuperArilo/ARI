package com.tty.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.annotations.function_type.FunctionHandlerRegistry;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.state.EditGuiState;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.gui.home.HomeEditor;
import com.tty.gui.home.HomeList;
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

public class EditHomeListener extends OnGuiEditListener<HomeEditor> {

    public EditHomeListener(GuiType guiType) {
        super(Ari.instance, new FunctionHandlerRegistry(new FunctionRegistry()), guiType);
    }

    @Override
    public boolean onTitleEditStatus(String message, EditGuiState state) {
        Player player = (Player) state.getOwner();
        List<Object> checkList = Ari.C_INSTANCE
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
        if(message.length() > Ari.C_INSTANCE.getValue("main.name-length", FilePath.HOME_CONFIG, Integer.class, 15)) {
            player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
            return false;
        }
        HomeEditor homeEditor = (HomeEditor) state.getI();
        homeEditor.getCurrentEditHome().setHomeName(message);
        Ari.SCHEDULER.runAtEntity(Ari.instance, player, p -> player.openInventory(homeEditor.getInventory()), () -> {});
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

    private static final class FunctionRegistry {

        @FunctionHandler(FunctionType.REBACK)
        public void onReback(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {
            event.getInventory().close();
            player.openInventory(new HomeList(player).getInventory());
        }

        @FunctionHandler(FunctionType.DELETE)
        public void onDelete(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {
            LambdaQueryWrapper<ServerHome> wrapper = new LambdaQueryWrapper<>(ServerHome.class)
                    .eq(ServerHome::getHomeId, holder.getCurrentEditHome().getHomeId())
                    .eq(ServerHome::getPlayerUUID, holder.getCurrentEditHome().getPlayerUUID());

            EntityRepository<ServerHome> repository = Ari.REPOSITORY_MANAGER.get(ServerHome.class);
            repository.delete(wrapper, PartitionKey.of(player.getUniqueId().toString()))
                    .thenCompose(success -> {
                        if (success) {
                            return ConfigUtils.t("function.home.delete-success", player)
                                    .thenAccept(player::sendMessage)
                                    .thenRun(() -> Ari.SCHEDULER.run(Ari.instance, j -> {
                                        event.getInventory().close();
                                        player.openInventory(new HomeList(player).getInventory());
                                    }));
                        } else {
                            return ConfigUtils.t("function.home.not-found", player).thenAccept(player::sendMessage);
                        }
                    });
        }

        @FunctionHandler(FunctionType.RENAME)
        public void onRename(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {
            Ari.STATE_MACHINE_MANAGER
                    .get(GuiEditStateService.class)
                    .addState(new EditGuiState(
                            player,
                            Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new com.google.common.reflect.TypeToken<Integer>(){}.getType()),
                            new HomeEditor(PublicFunctionUtils.deepCopy(holder.getCurrentEditHome(), ServerHome.class), player),
                            type)
                    );
            event.getInventory().close();
        }

        @FunctionHandler(FunctionType.LOCATION)
        public void onLocation(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            Location newLocation = player.getLocation();
            holder.getCurrentEditHome().setLocation(newLocation.toString());
            clickMeta.displayName(ComponentUtils.text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
            clickItem.setItemMeta(clickMeta);
        }

        @FunctionHandler(FunctionType.ICON)
        public void onIcon(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {

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
            holder.getCurrentEditHome().setShowMaterial(current.name());
        }

        @FunctionHandler(FunctionType.TOP_SLOT)
        public void onTopSlot(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            holder.getCurrentEditHome().setTopSlot(!holder.getCurrentEditHome().isTopSlot());
            holder.getBaseMenu().getFunctionItems().forEach((k, v) -> {
                if (v.getType().equals(FunctionType.TOP_SLOT)) {
                    List<String> lore = v.getLore();
                    List<TextComponent> list = lore.stream().map(p -> ComponentUtils.text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), ComponentUtils.text(Ari.DATA_SERVICE.getValue(holder.getCurrentEditHome().isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                    clickMeta.lore(list);
                    clickItem.setItemMeta(clickMeta);
                }
            });
        }

        @FunctionHandler(FunctionType.SAVE)
        public void onSave(FunctionType type, InventoryClickEvent event, HomeEditor holder, Player player) {

            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.ing"))));
            clickItem.setItemMeta(clickMeta);

            LambdaQueryWrapper<ServerHome> wrapper = new LambdaQueryWrapper<>(ServerHome.class)
                    .eq(ServerHome::getHomeId, holder.getCurrentEditHome().getHomeId())
                    .eq(ServerHome::getPlayerUUID, holder.getCurrentEditHome().getPlayerUUID());

            EntityRepository<ServerHome> repository = Ari.REPOSITORY_MANAGER.get(ServerHome.class);

            repository.update(holder.getCurrentEditHome(), wrapper, PartitionKey.of(player.getUniqueId().toString())).thenAccept(status -> {
                clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue(status ? "base.save.done":"base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                Ari.SCHEDULER.runAsyncDelayed(Ari.instance, e -> {
                    clickMeta.lore(List.of());
                    clickItem.setItemMeta(clickMeta);
                }, 20);
            }).exceptionally(i -> {
                Ari.LOG.error(i, "save home error");
                clickMeta.lore(List.of(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.save.error"))));
                clickItem.setItemMeta(clickMeta);
                player.sendMessage(Ari.DATA_SERVICE.getValue("base.on-error"));
                return null;
            });
        }

    }

}
