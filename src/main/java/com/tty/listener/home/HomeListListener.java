package com.tty.listener.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.api.enumType.GuiType;
import com.tty.gui.home.HomeEditor;
import com.tty.gui.home.HomeList;
import com.tty.api.Log;
import com.tty.api.enumType.FunctionType;
import com.tty.enumType.TeleportType;
import com.tty.api.FormatUtils;
import com.tty.api.listener.BaseGuiListener;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.CompletableFuture;


public class HomeListListener extends BaseGuiListener {

    private final NamespacedKey homeIdKey = new NamespacedKey(Ari.instance, "home_id");

    public HomeListListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        HomeList homeList = (HomeList) inventory.getHolder();
        ItemStack currentItem = event.getCurrentItem();
        assert currentItem != null;

        assert homeList != null;
        FunctionType type = this.ItemNBT_TypeCheck(currentItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Ari.instance, "type"), PersistentDataType.STRING));
        if(type == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        switch (type) {
            case BACK -> inventory.close();
            case DATA -> {
                String homeId = currentItem.getItemMeta().getPersistentDataContainer().get(this.homeIdKey, PersistentDataType.STRING);
                if (homeId == null) break;
                Ari.REPOSITORY_MANAGER.get(ServerHome.class).get(new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getHomeId, homeId).eq(ServerHome::getPlayerUUID, player.getUniqueId().toString()))
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
                                inventory.close();
                                player.openInventory(new HomeEditor(home, player).getInventory());
                            });
                        }
                        return CompletableFuture.completedFuture(true);
                    }).whenComplete((i, ex) -> {
                       if (ex != null) {
                           Log.error(ex, "error on get player homes");
                       }
                        Ari.SCHEDULER.run(Ari.instance, o -> inventory.close());
                    });
            }
            case PREV -> homeList.prev();
            case NEXT -> homeList.next();
        }
    }
}
