package com.tty.ari.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.ari.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.FunctionType;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.gui.home.HomeEditor;
import com.tty.ari.gui.home.HomeList;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;


public class HomeListListener extends BaseGuiListener<HomeList> {

    private final NamespacedKey homeIdKey = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_RENDER_DATA_ID);

    public HomeListListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {

    }

    @Override
    protected @NotNull FunctionHandler<HomeList> registry() {
        FunctionHandler<HomeList> registry = new FunctionHandler<>();

        registry.add(FunctionType.BACK, ((event, homeList, player) -> event.getInventory().close()));
        registry.add(FunctionType.DATA, (event, homeList, player) -> {

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            String homeId = currentItem.getItemMeta().getPersistentDataContainer().get(this.homeIdKey, PersistentDataType.STRING);
            if (homeId == null) return;
            Ari.REPOSITORY_MANAGER
                    .get(ServerHome.class)
                    .get(new LambdaQueryWrapper<>(ServerHome.class)
                                    .eq(ServerHome::getHomeId, homeId)
                                    .eq(ServerHome::getPlayerUUID, player.getUniqueId().toString()),
                            PartitionKey.of(player.getUniqueId().toString())
                    )
                    .thenCompose(home -> {
                        if (home == null) {
                            return ConfigUtils.t("function.home.not-found", player)
                                    .thenAccept(player::sendMessage)
                                    .thenApply(v -> false);
                        }
                        if (event.isLeftClick()) {
                            Ari.STATE_MACHINE_MANAGER
                                    .get(TeleportStateService.class)
                                    .addState(new EntityToLocationState(
                                            player,
                                            Ari.instance.getConfigInstance().getValue("main.teleport.delay", FilePath.HOME_CONFIG, Integer.class, 3),
                                            FormatUtils.parseLocation(home.getLocation()),
                                            TeleportType.HOME));
                        } else if (event.isRightClick()) {
                            Ari.instance.getScheduler().run(Ari.instance, p -> {
                                event.getInventory().close();
                                player.openInventory(new HomeEditor(home, player).getInventory());
                            });
                        }
                        return CompletableFuture.completedFuture(true);
                    }).whenComplete((i, ex) -> {
                        if (ex != null) {
                            Ari.instance.getLog().error(ex, "error on get player homes");
                        }
                        Ari.instance.getScheduler().run(Ari.instance, o -> event.getInventory().close());
                    });
        });
        registry.add(FunctionType.PREV_PAGE, (event, homeList, player) -> homeList.prev());
        registry.add(FunctionType.NEXT_PAGE, (event, homeList, player) -> homeList.next());

        return registry;
    }

    @Override
    protected boolean cancelClick(InventoryClickEvent event, HomeList holder) {
        return false;
    }

    @Override
    protected boolean cancelDrag(InventoryDragEvent event, HomeList holder) {
        return false;
    }

}
