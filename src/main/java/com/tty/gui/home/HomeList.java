package com.tty.gui.home;

import com.tty.Ari;
import com.tty.dto.CustomInventoryHolder;
import com.tty.entity.menu.BaseDataMenu;
import com.tty.entity.menu.FunctionItems;
import com.tty.entity.menu.Mask;
import com.tty.entity.sql.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.enumType.GuiType;
import com.tty.function.HomeManager;
import com.tty.gui.BaseDataItemInventory;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.enum_type.IconKeyType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeList extends BaseDataItemInventory<ServerHome> {

    public HomeList(Player player) {
        super(FormatUtils.yamlConvertToObj(Ari.C_INSTANCE.getObject(FilePath.HOME_LIST_GUI.name()).saveToString(), BaseDataMenu.class), player);
    }

    @Override
    public CompletableFuture<List<ServerHome>> requestData() {
        return new HomeManager(this.player, true)
                .getList(this.pageNum, this.baseDataInstance.getDataItems().getSlot().size());
    }

    @Override
    protected Map<Integer, ItemStack> getRenderItem() {
        Map<Integer, ItemStack> map = new HashMap<>();
        List<Integer> dataSlot = this.baseDataInstance.getDataItems().getSlot();
        List<String> rawLore = this.baseDataInstance.getDataItems().getLore();
        for (int i = 0; i < this.data.size(); i++) {
            ServerHome ph = this.data.get(i);
            ItemStack itemStack;
            try {
                itemStack = new ItemStack(Material.valueOf(ph.getShowMaterial().toUpperCase()));
            } catch (Exception e) {
                Log.error("There is a problem with the homeID: [%s] of the player: [%s]", ph.getHomeId(), this.player.getName());
                Log.warn("Skip the rendering homeId [%s] process...", ph.getHomeId());
                continue;
            }

            List<TextComponent> textComponents = new ArrayList<>();
            Location location = FormatUtils.parseLocation(ph.getLocation());

            for (String line : rawLore) {
                Map<String, Component> replacements = new HashMap<>();

                for (IconKeyType keyType : IconKeyType.values()) {
                    switch (keyType) {
                        case ID -> replacements.put(keyType.getKey(), ComponentUtils.text(ph.getHomeId()));
                        case X -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getX())));
                        case Y -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getY())));
                        case Z -> replacements.put(keyType.getKey(), ComponentUtils.text(FormatUtils.formatTwoDecimalPlaces(location.getZ())));
                        case WORLD_NAME -> replacements.put(keyType.getKey(), ComponentUtils.text(location.getWorld().getName()));
                    }
                }
                textComponents.add(ComponentUtils.text(line, replacements));
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentUtils.text(ph.getHomeName(), this.player));
            itemMeta.lore(textComponents);
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(Ari.instance, "home_id"), PersistentDataType.STRING, ph.getHomeId());
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(Ari.instance, "type"), PersistentDataType.STRING, FunctionType.DATA.name());
            if (ph.isTopSlot()) {
                itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);
            map.put(dataSlot.get(i), itemStack);
        }
        return map;
    }

    @Override
    protected Mask renderCustomMasks() {
        return null;
    }

    @Override
    protected Map<String, FunctionItems> renderCustomFunctionItems() {
        return null;
    }

    @Override
    protected CustomInventoryHolder createHolder() {
        return new CustomInventoryHolder(player, this.inventory, GuiType.HOME_LIST, this);
    }

}
