package com.tty.ari.gui;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.ari.Ari;
import com.tty.ari.dto.gui.PlayerInventoryCheckMenu;
import com.tty.ari.enumType.FilePath;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GuiMeta(type = "player_inventory_edit")
public class PlayerInventoryEdit extends BaseConfigInventory {

    @Getter
    private OfflinePlayer monitoree;
    private PlayerInventoryCache cache;

    public static final int MAX_PLAYER_INVENTORY_INDEX = 35;

    @Getter
    private final List<Integer> combineInventory = new ArrayList<>();

    public PlayerInventoryEdit(AbstractJavaPlugin plugin, Player owner,  OfflinePlayer monitoree) {
        super(plugin, owner);
        this.monitoree = monitoree;
        PlayerInventoryCheckMenu menu = (PlayerInventoryCheckMenu) this.getBaseMenu();
        List<Integer> shortcutBar = menu.getShortcutBar();
        List<Integer> playerInventory = menu.getPlayerInventory();
        if (shortcutBar.size() + playerInventory.size() != MAX_PLAYER_INVENTORY_INDEX + 1) throw new IllegalArgumentException("size not allowed");
        this.combineInventory.addAll(shortcutBar);
        this.combineInventory.addAll(playerInventory);
    }

    @Override
    protected @NotNull Component title() {
        return Ari.instance.getComponentTool().text(this.getBaseMenu().getTitle(), this.monitoree);
    }

    @Override
    protected @NotNull BaseMenu config() {
        return Ari.instance.getConfigInstance().yamlConvertToObj(Ari.instance.getConfigInstance().getObject(FilePath.INV_GUI_CONFIG).saveToString(), PlayerInventoryCheckMenu.class);
    }

    @Override
    protected void beforeRenderMasksAsync(@Nullable Mask mask) {
        this.cache = this.get(this.getMonitoree());
    }

    @Override
    protected void beforeRenderFunctionItemsAsync(Map<String, FunctionItems> functionItems) {
        if(functionItems == null) return;
        for (FunctionItems value : functionItems.values()) {
            switch (value.getType()) {
                case PLAYER_HEAD -> {
                    ItemStack itemStack = ItemStack.of(Material.valueOf(value.getMaterial()));
                    if (!(itemStack.getItemMeta() instanceof SkullMeta skullMeta)) break;
                    skullMeta.setPlayerProfile(this.monitoree.getPlayerProfile());
                    skullMeta.displayName(Ari.instance.getComponentTool().text(this.monitoree.getName()));
                    itemStack.setItemMeta(skullMeta);
                    value.setItemStack(itemStack);
                }
                case PLAYER_OFF_HAND -> {
                    ItemStack offHand = this.cache.getOff_hand();
                    if (offHand == null || offHand.isEmpty()) break;
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
    }

    @Override
    protected void whenRenderComplete(@NotNull Inventory inventory) {
        if (this.cache == null) return;
        Map<Integer, ItemStack> items = this.cache.getItems();
        items.forEach(inventory::setItem);
    }

    public @Nullable ItemStack getOffhand() {
        FunctionItems items = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_OFF_HAND)).findFirst().orElse(null);
        return this.getItemStackAndCheck(items);
    }

    public @Nullable ItemStack getOffhand(boolean hasNBT) {
        ItemStack offhand = this.getOffhand();
        if (offhand == null) return null;
        if(hasNBT) return offhand;
        Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, offhand);
        return offhand;
    }

    public void setOffhand(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_OFF_HAND, itemStack);
    }

    public @Nullable ItemStack getHelmet() {
        FunctionItems items = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_HELMET)).findFirst().orElse(null);
        return this.getItemStackAndCheck(items);
    }

    public @Nullable ItemStack getHelmet(boolean hasNBT) {
        ItemStack helmet = this.getHelmet();
        if (helmet == null) return null;
        if (hasNBT) return helmet;
        Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, helmet);
        return helmet;
    }

    public void setHelmet(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_HELMET, itemStack);
    }

    public @Nullable ItemStack getChestplate() {
        FunctionItems items = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_CHESTPLATE)).findFirst().orElse(null);
        return this.getItemStackAndCheck(items);
    }

    public @Nullable ItemStack getChestplate(boolean hasNBT) {
        ItemStack chestplate = this.getChestplate();
        if (chestplate == null) return null;
        if (hasNBT) return chestplate;
        Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, chestplate);
        return chestplate;
    }

    public void setChestplate(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_CHESTPLATE, itemStack);
    }

    public @Nullable ItemStack getLeggings() {
        FunctionItems items = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_LEGGINGS)).findFirst().orElse(null);
        return this.getItemStackAndCheck(items);
    }

    public @Nullable ItemStack getLeggings(boolean hasNBT) {
        ItemStack leggings = this.getLeggings();
        if (leggings == null) return null;
        if (hasNBT) return leggings;
        Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, leggings);
        return leggings;
    }

    public void setLeggings(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_LEGGINGS, itemStack);
    }

    public @Nullable ItemStack getBoots() {
        FunctionItems items = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_BOOTS)).findFirst().orElse(null);
        return this.getItemStackAndCheck(items);
    }

    public @Nullable ItemStack getBoots(boolean hasNBT) {
        ItemStack boots = this.getBoots();
        if (boots == null) return null;
        if (hasNBT) return boots;
        Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, boots);
        return boots;
    }

    public void setBoots(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_BOOTS, itemStack);
    }

    private void setEquipment(FunctionType type, @Nullable ItemStack itemStack) {
        FunctionItems items = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(type)).findFirst().orElse(null);
        if (items == null) return;
        if (itemStack == null) {
            itemStack = ItemStack.of(Material.valueOf(items.getMaterial()));
            Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, type.getName());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Ari.instance.getComponentTool().text(items.getName(), this.getOfflinePlayer()));
            itemStack.setItemMeta(itemMeta);
        }
        for (Integer i : items.getSlot()) {
            this.getInventory().setItem(i, itemStack);
        }
    }

    public void setItem(int index, @Nullable ItemStack stack) {
        if (index <= MAX_PLAYER_INVENTORY_INDEX) {
            this.getInventory().setItem(this.combineInventory.get(index), stack);
        } else {
            int offset = index - MAX_PLAYER_INVENTORY_INDEX;
            switch (offset) {
                case 1: this.setBoots(stack); break;
                case 2: this.setLeggings(stack); break;
                case 3: this.setChestplate(stack); break;
                case 4: this.setHelmet(stack); break;
                case 5: this.setOffhand(stack); break;
                default:
            }
        }
    }

    @Nullable
    private ItemStack getItemStackAndCheck(FunctionItems items) {
        if (items == null) return null;
        ItemStack item = this.getInventory().getItem(items.getSlot().getFirst());
        if (item == null) return null;
        if (item.getType().name().equalsIgnoreCase(items.getMaterial())) return null;
        return item;
    }

    @Override
    protected void onClose() {
        if (this.getMonitoree() instanceof Player player && player.isOnline()) return;

        NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(this.getMonitoree().getUniqueId().toString());
        if (data == null) {
            Ari.instance.getLog().error("can not open player {} inventory", this.getMonitoree().getName());
            return;
        }

        ReadWriteNBT equipment = data.getCompound("equipment");

        if (equipment == null) {
            equipment = data.getOrCreateCompound("equipment");
        }

        //读取箱子GUI里的装备
        ItemStack offhand = this.getOffhand(false);
        if (offhand != null) {
            equipment.setItemStack("offhand", offhand);
        } else {
            equipment.removeKey("offhand");
        }
        ItemStack helmet = this.getHelmet(false);
        if (helmet != null) {
            equipment.setItemStack("head", helmet);
        } else {
            equipment.removeKey("head");
        }
        ItemStack chestplate = this.getChestplate(false);
        if (chestplate != null) {
            equipment.setItemStack("chest", chestplate);
        } else {
            equipment.removeKey("chest");
        }
        ItemStack leggings = this.getLeggings(false);
        if (leggings != null) {
            equipment.setItemStack("legs", leggings);
        } else {
            equipment.removeKey("legs");
        }
        ItemStack boots = this.getBoots(false);
        if (boots != null) {
            equipment.setItemStack("feet", boots);
        } else {
            equipment.removeKey("feet");
        }

        data.removeKey("Inventory");
        ReadWriteNBTCompoundList inventory = data.getCompoundList("Inventory");
        for (int i = 0; i < this.combineInventory.size(); i++) {
            ItemStack item = this.getInventory().getItem(this.combineInventory.get(i));
            if (item == null || item.getType().isAir()) continue;
            try {
                ReadWriteNBT readWriteNBT = NBT.itemStackToNBT(item);
                readWriteNBT.setByte("Slot", (byte) i);
                inventory.addCompound(readWriteNBT);
            } catch (Exception e) {
                this.getPlugin().getLog().error("Invalid item in player data, skipping slot {}", i);
            }
        }

        try {
            data.save();
            this.getPlugin().getLog().debug("saved player {} inventory.", this.monitoree.getName());
        } catch (IOException e) {
            this.getPlugin().getLog().error("can not open player {} inventory", this.getMonitoree().getName());
        } finally {
            this.cache = null;
            this.monitoree = null;
        }
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
                cache.setChestplate(chestplate.clone());
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
            for (int i = 0; i < inventory.getSize(); i++) {
                if (i > MAX_PLAYER_INVENTORY_INDEX) break;
                ItemStack itemStack = contents[i];
                if (itemStack == null) continue;
                ItemStack clone = itemStack.clone();
                cache.addItem(this.combineInventory.get(i), clone);
                this.getPlugin().getLog().debug("index: {}, item: {}", i, itemStack.getType().name());
            }
        } else {
            NBTFileHandle data = Ari.NBT_DATA_SERVICE.getData(offlinePlayer.getUniqueId().toString());
            if (data == null) {
                Ari.instance.getLog().debug("can not find player {} data.", this.getMonitoree().getName());
                return null;
            }

            ReadWriteNBTCompoundList inventory = data.getCompoundList("Inventory");

            for (ReadWriteNBT item : inventory) {
                int slot = item.getByte("Slot") & 0xFF;
                cache.addItem(this.combineInventory.get(slot), NBT.itemStackFromNBT(item));
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

    public static class PlayerInventoryCache {

        @Nullable
        @Getter
        private ItemStack off_hand;

        @Setter
        @Nullable
        @Getter
        private ItemStack helmet;

        @Setter
        @Nullable
        @Getter
        private ItemStack chestplate;

        @Setter
        @Nullable
        @Getter
        private ItemStack leggings;

        @Setter
        @Nullable
        @Getter
        private ItemStack boots;

        @Getter
        private final Map<Integer, ItemStack> items = new HashMap<>();

        public void setOffHand(ItemStack itemStack) {
            this.off_hand = itemStack;
        }

        public void addItem(int slot, ItemStack stack) {
            this.items.put(slot,stack);
        }

    }

}
