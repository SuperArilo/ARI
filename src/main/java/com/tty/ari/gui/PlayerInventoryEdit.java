package com.tty.ari.gui;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseDataMenu;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.FunctionType;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.api.utils.FormatUtils;
import com.tty.api.utils.GuiNBTKeys;
import com.tty.ari.Ari;
import com.tty.ari.dto.PlayerInventoryCache;
import com.tty.ari.enumType.FilePath;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "player_inventory_edit")
public class PlayerInventoryEdit extends BaseConfigInventory {

    private PlayerInventoryCache cache;

    public static final List<Integer> PLAYER_INVENTORY_SLOT_MAP = List.of(
            45, 46, 47, 48, 49, 50, 51, 52, 53,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private final NamespacedKey equipmentKey;

    public PlayerInventoryEdit(AbstractJavaPlugin plugin, OfflinePlayer target) {
        super(plugin, target);
        this.equipmentKey = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_EQUIPMENT_TYPE);
    }

    @Override
    protected @NotNull BaseMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.INV_GUI_CONFIG.name()).saveToString(), BaseDataMenu.class);
    }

    @Override
    protected @NotNull CompletableFuture<Mask> beforeRenderMasksAsync(@Nullable Mask mask) {
        this.cache = this.get(this.getOfflinePlayer());
        return CompletableFuture.completedFuture(mask);
    }

    @Override
    protected @NotNull CompletableFuture<Map<String, FunctionItems>> beforeRenderFunctionItemsAsync(Map<String, FunctionItems> functionItems) {
        if(functionItems == null) return CompletableFuture.completedFuture(null);
        for (FunctionItems value : functionItems.values()) {
            switch (value.getType()) {
                case PLAYER_OFF_HAND -> {
                    ItemStack offHand = this.cache.getOff_hand();
                    if (offHand == null || offHand.isEmpty()) break;
                    ItemMeta itemMeta = offHand.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(this.equipmentKey, PersistentDataType.STRING, FunctionType.PLAYER_OFF_HAND.name());

                    value.setItemStack(offHand);
                }
                case PLAYER_HELMET -> {
                    ItemStack helmet = this.cache.getHelmet();
                    if (helmet == null || helmet.isEmpty()) break;
                    value.setItemStack(helmet);
                }
                case PLAYER_CHESTPLATE -> {
                    ItemStack chestplate = this.cache.getChestplate();
                    if (chestplate == null || chestplate.isEmpty()) break;
                    value.setItemStack(chestplate);
                }
                case PLAYER_LEGGINGS -> {
                    ItemStack leggings = this.cache.getLeggings();
                    if (leggings == null || leggings.isEmpty()) break;
                    value.setItemStack(leggings);
                }
                case PLAYER_BOOTS -> {
                    ItemStack boots = this.cache.getBoots();
                    if (boots == null || boots.isEmpty()) break;
                    value.setItemStack(boots);
                }
            }
        }
        return CompletableFuture.completedFuture(functionItems);
    }

    @Override
    protected void whenRenderComplete(@NotNull Inventory inventory) {
        if (this.cache == null) return;
        Map<Integer, ItemStack> items = this.cache.getItems();
        items.forEach(inventory::setItem);
    }

    private @Nullable PlayerInventoryCache get(OfflinePlayer offlinePlayer) {
        PlayerInventoryCache cache = new PlayerInventoryCache();
        if (offlinePlayer instanceof Player player) {
            PlayerInventory inventory = player.getInventory();

            cache.setOffHand(inventory.getItemInOffHand().clone());
            ItemStack helmet = inventory.getHelmet();
            if (helmet != null) {
                cache.setHelmet(helmet.clone());
            }
            ItemStack chestplate = inventory.getChestplate();
            if (chestplate != null) {
                cache.setChestplate(chestplate);
            }
            ItemStack leggings = inventory.getLeggings();
            if (leggings != null) {
                cache.setLeggings(leggings.clone());
            }
            ItemStack boots = inventory.getBoots();
            if (boots != null) {
                cache.setBoots(boots.clone());
            }

            @Nullable ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < PLAYER_INVENTORY_SLOT_MAP.size(); i++) {
                ItemStack itemStack = contents[i];
                if (itemStack == null) continue;
                cache.addItem(PLAYER_INVENTORY_SLOT_MAP.get(i), itemStack.clone());
                this.getLog().debug("index: {}, item: {}", i, itemStack.getType().name());
            }
        } else {
            NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(offlinePlayer.getUniqueId().toString());
            if (data == null) {
                Ari.instance.getLog().debug("can not find player {} data.", this.getOfflinePlayer().getName());
                return null;
            }

            ReadWriteNBTCompoundList inventory = data.getCompoundList("Inventory");

            for (ReadWriteNBT item : inventory) {
                int slot = item.getByte("Slot") & 0xFF;
                if (slot < PLAYER_INVENTORY_SLOT_MAP.size()) {
                    cache.addItem(PLAYER_INVENTORY_SLOT_MAP.get(slot), NBT.itemStackFromNBT(item));
                }
            }

            if (data.hasTag("equipment")) {
                ReadWriteNBT equipment = data.getCompound("equipment");
                if (equipment == null) return null;

                if (equipment.hasTag("offhand")) {
                    ReadWriteNBT offhandItem = equipment.getCompound("offhand");
                    if (offhandItem != null) {
                        cache.setOffHand(NBT.itemStackFromNBT(offhandItem));
                    }
                }

                if (equipment.hasTag("head")) {
                    ReadWriteNBT head = equipment.getCompound("head");
                    if (head != null) {
                        cache.setHelmet(NBT.itemStackFromNBT(head));
                    }
                }

                if (equipment.hasTag("chest")) {
                    ReadWriteNBT chest = equipment.getCompound("chest");
                    if (chest != null) {
                        cache.setChestplate(NBT.itemStackFromNBT(chest));
                    }
                }

                if (equipment.hasTag("legs")) {
                    ReadWriteNBT legs = equipment.getCompound("legs");
                    if (legs != null) {
                        cache.setLeggings(NBT.itemStackFromNBT(legs));
                    }
                }

                if (equipment.hasTag("feet")) {
                    ReadWriteNBT feet = equipment.getCompound("feet");
                    if (feet != null) {
                        cache.setBoots(NBT.itemStackFromNBT(feet));
                    }
                }
            }

        }

        return cache;
    }

    @Override
    protected void cleanAsync() {
        if (!(this.getOfflinePlayer() instanceof Player)) {
            NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(this.getOfflinePlayer().getUniqueId().toString());
            if (data == null) {
                Ari.instance.getLog().error("can not open player {} inventory", this.getOfflinePlayer().getName());
                return;
            }

            ReadWriteNBT equipment = data.getCompound("equipment");

            if (equipment == null) {
                equipment = data.getOrCreateCompound("equipment");
            }

            for (FunctionItems value : this.getBaseMenu().getFunctionItems().values()) {
                ItemStack itemStack = this.getInventory().getItem(value.getSlot().getFirst());

                if (itemStack == null || itemStack.getType().name().equalsIgnoreCase(value.getMaterial())) continue;
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.getPersistentDataContainer().remove(this.equipmentKey);
                itemStack.setItemMeta(itemMeta);

                switch (value.getType()) {
                    case PLAYER_OFF_HAND -> equipment.setItemStack("offhand", itemStack);
                    case PLAYER_HELMET -> equipment.setItemStack("head", itemStack);
                    case PLAYER_CHESTPLATE -> equipment.setItemStack("chest", itemStack);
                    case PLAYER_LEGGINGS -> equipment.setItemStack("legs", itemStack);
                    case PLAYER_BOOTS -> equipment.setItemStack("feet", itemStack);
                }
            }


            //填充背包
            data.removeKey("Inventory");

            ReadWriteNBTCompoundList inventory = data.getCompoundList("Inventory");

            int i = this.getInventory().getSize() - PLAYER_INVENTORY_SLOT_MAP.size();
            for (int slot : PLAYER_INVENTORY_SLOT_MAP) {
                ItemStack item = this.getInventory().getItem(slot);
                if (item == null || item.getType().isAir()) continue;
                ReadWriteNBT readWriteNBT = NBT.itemStackToNBT(item);
                readWriteNBT.setByte("Slot", (byte) (slot - i));
                inventory.addCompound(readWriteNBT);
            }
            try {
                data.save();
            } catch (IOException e) {
                Ari.instance.getLog().error("can not open player {} inventory", this.getOfflinePlayer().getName());
            }
        }
        super.cleanAsync();
        this.cache = null;
    }

}
