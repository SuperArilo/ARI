package com.tty.ari.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.ari.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.enumType.FunctionType;
import com.tty.api.listener.BaseGuiListener;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.gui.home.HomeEditor;
import com.tty.ari.gui.home.HomeList;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class HomeListListener extends BaseGuiListener<HomeList> {

    public HomeListListener(GuiType guiType) {
        super(Ari.instance, guiType);
    }

    @Override
    protected @NotNull FunctionHandler<HomeList> registry() {
        FunctionHandler<HomeList> registry = new FunctionHandler<>();

        registry.add(FunctionType.BACK, ((event, homeList, player) -> event.getInventory().close()));
        registry.add(FunctionType.DATA, (event, homeList, player) -> {

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            String homeId = Ari.instance.getNbtManager().getNbt(NbtGuiValue.GUI_DATA_ID, currentItem, PersistentDataType.STRING);
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
                            Ari.instance.getScheduler().runAtEntity(Ari.instance, player, p -> {
                                event.getInventory().close();
                                Ari.instance.getScheduler().run(Ari.instance, i-> Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new GuiState(player, new HomeEditor(home, player))));
                            }, null);
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
    protected void whenClick(InventoryClickEvent event, HomeList holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenShiftClick(InventoryClickEvent event, HomeList holder) {
        event.setCancelled(true);
    }

    @Override
    protected void whenDrag(InventoryDragEvent event, HomeList holder) {
        event.setCancelled(true);
    }

}
