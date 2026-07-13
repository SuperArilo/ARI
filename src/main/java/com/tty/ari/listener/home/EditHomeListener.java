package com.tty.ari.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.gui.AsyncGuiClick;
import com.tty.api.repository.PartitionKey;
import com.tty.api.state.GuiEditFunctionState;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.home.HomeConfig;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.gui.home.HomeEditor;
import com.tty.ari.gui.home.HomeList;
import com.tty.ari.listener.OnGuiEditListener;
import com.tty.ari.states.gui.GuiEditFunctionStateService;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
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

public class EditHomeListener extends OnGuiEditListener<HomeEditor, ServerHome> implements AsyncGuiClick {

    public EditHomeListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    public boolean onTitleEditStatus(String message, GuiEditFunctionState<ServerHome> state) {
        Player player = (Player) state.getOwner();

        HomeConfig homeConfig = Ari.instance.getConfigurationManager().get(HomeConfig.class);

        if(!this.isContentValid(message) || homeConfig.getCheckHomeName().contains(message)) {
            player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-error")));
            return false;
        }
        if(message.length() > homeConfig.getHomeNameLength()) {
            player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.rename.name-too-long")));
            return false;
        }
        ServerHome data = state.getData();
        data.setHomeName(message);
        Ari.instance.getStatusManager().get(GuiManagerStateService.class).addState(new GuiState(player, new HomeEditor(player, data)));
        return true;
    }

    @Override
    public void whenTimeout(Player player) {
        player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
    }

    @Override
    protected @NotNull FunctionHandler<HomeEditor> registry() {
        FunctionHandler<HomeEditor> registry = new FunctionHandler<>();

        registry.addSync(FunctionType.REBACK, (event, homeEditor, player) -> {
            event.getInventory().close();
            Ari.instance.getStatusManager().get(GuiManagerStateService.class).addState(new GuiState(player, new HomeList(player)));
        });
        registry.addAsync(FunctionType.DELETE, (event, homeEditor, player) -> {
            LambdaQueryWrapper<ServerHome> wrapper = new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getHomeId, homeEditor.getHome().getHomeId()).eq(ServerHome::getPlayerUUID, homeEditor.getHome().getPlayerUUID());
            return Ari.REPOSITORY_MANAGER.get(ServerHome.class).delete(wrapper, PartitionKey.of(player.getUniqueId().toString())).thenCompose(count -> {
                if (count == 1) {
                    return ConfigUtils.t("function.home.delete-success", player).thenAccept(player::sendMessage).thenRun(() -> Ari.instance.getScheduler().run(j -> {
                        event.getInventory().close();
                        Ari.instance.getStatusManager().get(GuiManagerStateService.class).addState(new GuiState(player, new HomeList(player)));
                    }));
                } else {
                    return ConfigUtils.t("function.home.not-found", player).thenAccept(player::sendMessage);
                }
            });
        });
        registry.addSync(FunctionType.RENAME, (event, homeEditor, player) -> {
            event.getInventory().close();
            Ari.instance.getStatusManager().get(GuiEditFunctionStateService.class).addState(
                    new GuiEditFunctionState<>(
                            player,
                            Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new TypeToken<Integer>(){}.getType()),
                            homeEditor.getHome(),
                            FunctionType.RENAME,
                            GuiType.HOME_EDIT)
                    );
        });
        registry.addSync(FunctionType.LOCATION, ((event, homeEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;

            ItemMeta clickMeta = clickItem.getItemMeta();
            Location newLocation = player.getLocation();
            homeEditor.getHome().setLocation(newLocation.toString());
            clickMeta.displayName(Ari.instance.getComponentTool().text(FormatUtils.XYZText(newLocation.getX(), newLocation.getY(), newLocation.getZ())));
            clickItem.setItemMeta(clickMeta);

        }));
        registry.addSync(FunctionType.ICON, (event, homeEditor, player) -> {
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
            homeEditor.getHome().setShowMaterial(current.name());

        });
        registry.addSync(FunctionType.TOP_SLOT, (event, homeEditor, player) -> {
            ItemStack clickItem = event.getCurrentItem();
            if (clickItem == null) return;
            ItemMeta clickMeta = clickItem.getItemMeta();

            homeEditor.getHome().setTopSlot(!homeEditor.getHome().isTopSlot());

            for (FunctionItems v : homeEditor.getBaseMenu().getFunctionItems().values()) {
                if (v.getType().equals(FunctionType.TOP_SLOT)) {
                    List<String> lore = v.getLore();
                    List<TextComponent> list = lore.stream().map(p -> Ari.instance.getComponentTool().text(p, Map.of(IconKeyType.TOP_SLOT.getKey(), Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue(homeEditor.getHome().isTopSlot() ? "base.yes_re" : "base.no_re"))))).toList();
                    clickMeta.lore(list);
                    clickItem.setItemMeta(clickMeta);
                }
            }
        });
        registry.addAsync(FunctionType.SAVE, (event, homeEditor, player) -> {
            LambdaQueryWrapper<ServerHome> wrapper = new LambdaQueryWrapper<>(ServerHome.class)
                    .eq(ServerHome::getHomeId, homeEditor.getHome().getHomeId())
                    .eq(ServerHome::getPlayerUUID, homeEditor.getHome().getPlayerUUID());

            return Ari.REPOSITORY_MANAGER.get(ServerHome.class).update(homeEditor.getHome(), wrapper, PartitionKey.of(player.getUniqueId().toString())).thenAccept(status -> {}).exceptionally(i -> {
                Ari.instance.getLog().error(i, "save home error");
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

    @Override
    public @NotNull Component whenPending() {
        return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.save.ing"));
    }

    @Override
    public @NotNull Component whenDone() {
        return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.save.done"));
    }

    @Override
    public @NotNull Component whenError() {
        return Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.save.error"));
    }

}
