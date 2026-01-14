package com.tty.listener.home;

import com.tty.Ari;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.GuiType;
import com.tty.function.HomeManager;
import com.tty.gui.home.HomeEditor;
import com.tty.gui.home.HomeList;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.TeleportType;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.listener.BaseGuiListener;
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
        FunctionType type = FormatUtils.ItemNBT_TypeCheck(currentItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Ari.instance, "type"), PersistentDataType.STRING));
        if(type == null) return;
        Player player = homeList.player;

        switch (type) {
            case BACK -> inventory.close();
            case DATA -> {
                String homeId = currentItem.getItemMeta().getPersistentDataContainer().get(this.homeIdKey, PersistentDataType.STRING);
                if (homeId == null) break;
                Ari.REPOSITORY_MANAGER.get(ServerHome.class).get(new HomeManager.QueryKey(player.getUniqueId().toString(), homeId))
                    .thenCompose(home -> {
                        if (home == null) {
                            player.sendMessage(ConfigUtils.t("function.home.not-found"));
                            return CompletableFuture.completedFuture(false);
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
                            Lib.Scheduler.run(Ari.instance, p -> {
                                inventory.close();
                                new HomeEditor(home, player).open();
                            });
                        }
                        return CompletableFuture.completedFuture(true);
                    }).whenComplete((i, ex) -> {
                       if (ex != null) {
                           Log.error(ex, "error on get player homes");
                       }
                        Lib.Scheduler.run(Ari.instance, o -> inventory.close());
                    });
            }
            case PREV -> homeList.prev();
            case NEXT -> homeList.next();
        }
    }
}
