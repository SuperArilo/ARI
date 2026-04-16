package com.tty.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.api.annotations.function_type.FunctionHandler;
import com.tty.api.annotations.function_type.FunctionHandlerRegistry;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.gui.home.HomeEditor;
import com.tty.gui.home.HomeList;
import com.tty.api.enumType.FunctionType;
import com.tty.enumType.TeleportType;
import com.tty.api.utils.FormatUtils;
import com.tty.api.listener.BaseGuiListener;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.CompletableFuture;


public class HomeListListener extends BaseGuiListener<HomeList> {

    public HomeListListener(GuiType guiType) {
        super(Ari.instance, new FunctionHandlerRegistry(new FunctionRegistry()), guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {

    }

    private static final class FunctionRegistry {

        private final NamespacedKey homeIdKey = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_RENDER_DATA_ID);

        @FunctionHandler(FunctionType.BACK)
        public void onBack(FunctionType type, InventoryClickEvent event, HomeList holder, Player player) {
            event.getInventory().close();
        }

        @FunctionHandler(FunctionType.DATA)
        public void onData(FunctionType type, InventoryClickEvent event, HomeList holder, Player player) {

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
                                            Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.HOME_CONFIG, Integer.class, 3),
                                            FormatUtils.parseLocation(home.getLocation()),
                                            TeleportType.HOME));
                        } else if (event.isRightClick()) {
                            Ari.SCHEDULER.run(Ari.instance, p -> {
                                event.getInventory().close();
                                player.openInventory(new HomeEditor(home, player).getInventory());
                            });
                        }
                        return CompletableFuture.completedFuture(true);
                    }).whenComplete((i, ex) -> {
                        if (ex != null) {
                            Ari.LOG.error(ex, "error on get player homes");
                        }
                        Ari.SCHEDULER.run(Ari.instance, o -> event.getInventory().close());
                    });
        }

        @FunctionHandler(FunctionType.PREV_PAGE)
        public void onPrevPage(FunctionType type, InventoryClickEvent event, HomeList holder, Player player) {
            holder.prev();
        }

        @FunctionHandler(FunctionType.NEXT_PAGE)
        public void onNextPage(FunctionType type, InventoryClickEvent event, HomeList holder, Player player) {
            holder.next();
        }

    }

}
