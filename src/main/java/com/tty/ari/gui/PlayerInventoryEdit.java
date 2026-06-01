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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@GuiMeta(type = "player_inventory_edit")
public class PlayerInventoryEdit extends BaseConfigInventory {

    private PlayerInventoryCache cache;

    public static final int[] PLAYER_INVENTORY_SLOT_MAP = new int[36];

    static {
        for (int i = 0; i <= 8; i++) PLAYER_INVENTORY_SLOT_MAP[i] = 45 + i;
        for (int i = 9; i <= 17; i++) PLAYER_INVENTORY_SLOT_MAP[i] = 36 + i - 9;
        for (int i = 18; i <= 26; i++) PLAYER_INVENTORY_SLOT_MAP[i] = 27 + i - 18;
        for (int i = 27; i <= 35; i++) PLAYER_INVENTORY_SLOT_MAP[i] = 18 + i - 27;
    }

    private final NamespacedKey equipmentKey;

    public PlayerInventoryEdit(AbstractJavaPlugin plugin, OfflinePlayer target) {
        super(plugin, target);
        this.cache = this.get(target);
        this.equipmentKey = new NamespacedKey(Ari.instance, GuiNBTKeys.GUI_EQUIPMENT_TYPE);
    }

    @Override
    protected @NotNull BaseMenu config() {
        return FormatUtils.yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.INV_GUI_CONFIG.name()).saveToString(), BaseDataMenu.class);
    }

    @Override
    protected @NotNull CompletableFuture<Mask> beforeRenderMasks(@Nullable Mask mask) {
        return CompletableFuture.completedFuture(mask);
    }

    @Override
    protected @NotNull CompletableFuture<Map<String, FunctionItems>> beforeRenderFunctionItems(Map<String, FunctionItems> functionItems) {
        if (this.cache == null) CompletableFuture.completedFuture(functionItems);
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
        ItemStack[] items = this.cache.getItems();
        if (items == null || items.length != 36) return;
        for (int i = 0; i < 36; i++) {
            inventory.setItem(PLAYER_INVENTORY_SLOT_MAP[i], items[i]);
        }
    }

    private @Nullable PlayerInventoryCache get(OfflinePlayer offlinePlayer) {
        PlayerInventoryCache cache = new PlayerInventoryCache();
        if (offlinePlayer instanceof Player player) {
            PlayerInventory inventory = player.getInventory();
            cache.setOffHand(inventory.getItemInOffHand())
                    .setChestplate(inventory.getChestplate())
                    .setLeggings(inventory.getLeggings())
                    .setBoots(inventory.getBoots())
                    .setItems(inventory.getArmorContents());
        } else {
            NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(offlinePlayer.getUniqueId().toString());
            if (data == null) {
                Ari.instance.getLog().debug("null");
                return null;
            }

            ReadWriteNBTCompoundList inventory = data.getCompoundList("Inventory");

            for (ReadWriteNBT item : inventory) {
                int slot = item.getByte("Slot") & 0xFF;
                if (slot < 36) {
                    cache.addItem(slot, NBT.itemStackFromNBT(item));
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
    protected void clean() {
        super.clean();
        this.cache = null;
    }

}
