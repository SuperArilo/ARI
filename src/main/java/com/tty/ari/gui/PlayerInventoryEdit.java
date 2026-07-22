package com.tty.ari.gui;

import com.tty.api.AbstractJavaPlugin;
import com.tty.api.ComponentTool;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.dto.gui.BaseMenu;
import com.tty.api.dto.gui.FunctionItems;
import com.tty.api.dto.gui.Mask;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.IconKeyType;
import com.tty.api.enumType.NbtGuiValue;
import com.tty.api.gui.BaseConfigInventory;
import com.tty.api.utils.FormatUtils;
import com.tty.ari.Ari;
import com.tty.ari.configuration.CheckInventoryLayoutConfig;
import com.tty.ari.dto.gui.PlayerInventoryCheckMenu;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
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
        return ComponentTool.text(this.getBaseMenu().getTitle(), this.monitoree);
    }

    @Override
    protected @NotNull BaseMenu config() {
        return Ari.instance.getConfigurationManager().get(CheckInventoryLayoutConfig.class).getMenuConfig(PlayerInventoryCheckMenu.class);
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
                    skullMeta.displayName(ComponentTool.text(this.monitoree.getName()));
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
                case PLAYER_EXPERIENCE -> {
                    Map<IconKeyType, String> map = Map.of(
                            IconKeyType.PLAYER_TOTAL_EXPERIENCE, FormatUtils.formatTwoDecimalPlaces(this.cache.getTotalExperience()),
                            IconKeyType.PLAYER_EXP, FormatUtils.formatTwoDecimalPlaces(this.cache.getExp()),
                            IconKeyType.PLAYER_LEVEL, FormatUtils.formatTwoDecimalPlaces(this.cache.getLevel())
                    );
                    value.setName(this.replaceKey(value.getName(), map));
                    List<String> lore = new ArrayList<>();
                    for (String string : value.getLore()) {
                        lore.add(this.replaceKey(string, map));
                    }
                    value.setLore(lore);
                }
                case LOCATION -> {
                    if (this.monitoree instanceof Player player) {
                        this.getPlugin().getScheduler().runAtEntity(player, i -> {
                            Location location = player.getLocation();

                            Map<IconKeyType, String> map = Map.of(
                                    IconKeyType.X, FormatUtils.formatTwoDecimalPlaces(location.getX()),
                                    IconKeyType.Y, FormatUtils.formatTwoDecimalPlaces(location.getY()),
                                    IconKeyType.Z, FormatUtils.formatTwoDecimalPlaces(location.getZ()),
                                    IconKeyType.WORLD_NAME, location.getWorld().getName()
                            );
                            List<String> list = new ArrayList<>();
                            for (String string : value.getLore()) {
                                list.add(this.replaceKey(string, map));
                            }
                            value.setLore(list);
                        }, null);
                    } else {
                        value.setLore(List.of());
                    }
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

    public @NotNull List<ItemStack> getOffhand() {
        return this.getEquipmentAndCheck(this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_OFF_HAND)).toList());
    }

    public @Nullable List<ItemStack> getOffhand(boolean hasNBT) {
        List<ItemStack> offhand = this.getOffhand();
        if(hasNBT) return offhand;

        for (ItemStack itemStack : offhand) {
            if (itemStack == null) continue;
            Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack);
        }

        return offhand;
    }

    public void setOffhand(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_OFF_HAND, itemStack);
    }

    public @NotNull List<ItemStack> getHelmet() {
        return this.getEquipmentAndCheck(this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_HELMET)).toList());
    }

    public @Nullable List<ItemStack> getHelmet(boolean hasNBT) {
        List<ItemStack> helmet = this.getHelmet();
        if (hasNBT) return helmet;
        for (ItemStack itemStack : helmet) {
            if (itemStack == null) continue;
            Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack);
        }
        return helmet;
    }

    public void setHelmet(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_HELMET, itemStack);
    }

    public @NotNull List<ItemStack> getChestplate() {
        return this.getEquipmentAndCheck(this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_CHESTPLATE)).toList());
    }

    public @Nullable List<ItemStack> getChestplate(boolean hasNBT) {
        List<ItemStack> chestplate = this.getChestplate();
        if (hasNBT) return chestplate;
        for (ItemStack itemStack : chestplate) {
            if (itemStack == null) continue;
            Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack);
        }
        return chestplate;
    }

    public void setChestplate(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_CHESTPLATE, itemStack);
    }

    public @NotNull List<ItemStack> getLeggings() {
        return this.getEquipmentAndCheck(this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_LEGGINGS)).toList());
    }

    public @Nullable List<ItemStack> getLeggings(boolean hasNBT) {
        List<ItemStack> leggings = this.getLeggings();
        if (hasNBT) return leggings;
        for (ItemStack itemStack : leggings) {
            if (itemStack == null) continue;
            Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack);
        }
        return leggings;
    }

    public void setLeggings(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_LEGGINGS, itemStack);
    }

    public @NotNull List<ItemStack> getBoots() {
        return this.getEquipmentAndCheck(this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_BOOTS)).toList());
    }

    public @Nullable List<ItemStack> getBoots(boolean hasNBT) {
        List<ItemStack> boots = this.getBoots();
        if (hasNBT) return boots;
        for (ItemStack itemStack : boots) {
            if (itemStack == null) continue;
            Ari.instance.getNbtManager().removeNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack);
        }
        return boots;
    }

    public void setBoots(@Nullable ItemStack itemStack) {
        this.setEquipment(FunctionType.PLAYER_BOOTS, itemStack);
    }

    public void setExperience(int total, float progress, int level) {
        List<FunctionItems> list = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.PLAYER_EXPERIENCE)).toList();

        Map<IconKeyType, String> map = new HashMap<>();
        map.put(IconKeyType.PLAYER_TOTAL_EXPERIENCE, FormatUtils.formatTwoDecimalPlaces(total));
        map.put(IconKeyType.PLAYER_EXP, FormatUtils.formatTwoDecimalPlaces(progress));
        map.put(IconKeyType.PLAYER_LEVEL, FormatUtils.formatTwoDecimalPlaces(level));

        for (FunctionItems item : list) {
            ItemStack itemStack = ItemStack.of(Material.valueOf(item.getMaterial().toUpperCase()));
            Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, FunctionType.PLAYER_EXPERIENCE.getName());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentTool.text(this.replaceKey(item.getName(), map), this.getOfflinePlayer()));
            List<Component> c = new ArrayList<>();
            for (String s : item.getLore()) {
                c.add(ComponentTool.text(this.replaceKey(s, map)));
            }
            itemMeta.lore(c);
            itemStack.setItemMeta(itemMeta);

            //这个 slot 是箱子gui里的所以不能用这个类里的 setAbstractItem
            for (Integer i : item.getSlot()) {
                this.getInventory().setItem(i, itemStack);
            }
        }
    }

    public void updateLocation(@Nullable Location location) {
        List<FunctionItems> list = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(FunctionType.LOCATION)).toList();

        Map<IconKeyType, String> map = null;
        if (location != null) {
            map = Map.of(
                    IconKeyType.X, FormatUtils.formatTwoDecimalPlaces(location.getX()),
                    IconKeyType.Y, FormatUtils.formatTwoDecimalPlaces(location.getY()),
                    IconKeyType.Z, FormatUtils.formatTwoDecimalPlaces(location.getZ()),
                    IconKeyType.WORLD_NAME, location.getWorld().getName()
            );
        }

        for (FunctionItems item : list) {
            ItemStack itemStack = ItemStack.of(Material.valueOf(item.getMaterial().toUpperCase()));
            Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, item.getType().name());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentTool.text(item.getName()));
            if (location != null) {
                List<TextComponent> components = new ArrayList<>();
                for (String string : item.getLore()) {
                    components.add(ComponentTool.text(this.replaceKey(string, map)));
                }
                itemMeta.lore(components);
            } else {
                itemMeta.lore(List.of());
            }

            itemStack.setItemMeta(itemMeta);
            for (Integer i : item.getSlot()) {
                this.getInventory().setItem(i, itemStack);
            }
        }

    }

    private void setEquipment(FunctionType type, @Nullable ItemStack itemStack) {
        List<FunctionItems> list = this.getBaseMenu().getFunctionItems().values().stream().filter(i -> i.getType().equals(type)).toList();
        if (list.isEmpty()) return;
        for (FunctionItems item : list) {
            if (itemStack == null) {
                itemStack = ItemStack.of(Material.valueOf(item.getMaterial()));
                Ari.instance.getNbtManager().setNbt(NbtGuiValue.GUI_FUNCTION_ICON, itemStack, PersistentDataType.STRING, type.getName());
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(ComponentTool.text(item.getName(), this.getOfflinePlayer()));
                itemStack.setItemMeta(itemMeta);
            }
            for (Integer i : item.getSlot()) {
                this.getInventory().setItem(i, itemStack);
            }
        }
    }

    //只能set装备、左手和玩家背包映射槽位
    public void setAbstractItem(int index, @Nullable ItemStack stack) {
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

    private @NotNull List<ItemStack> getEquipmentAndCheck(@Nullable List<FunctionItems> list) {
        if (list == null || list.isEmpty()) return List.of();

        List<ItemStack> stacks = new ArrayList<>();

        for (FunctionItems item : list) {
            List<Integer> slots = item.getSlot();
            for (Integer slot : slots) {
                ItemStack itemStack = this.getInventory().getItem(slot);
                if (itemStack == null) continue;
                if (itemStack.getType().name().equalsIgnoreCase(item.getMaterial())) continue;
                stacks.add(this.getInventory().getItem(slot));
            }
        }
        return stacks;
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
        List<ItemStack> offhand = this.getOffhand(false);
        if (offhand == null || offhand.isEmpty()) {
            equipment.removeKey("offhand");
        } else {
            for (ItemStack stack : offhand) {
                if (stack != null) {
                    equipment.setItemStack("offhand", stack);
                    break;
                }
            }
        }

        List<ItemStack> helmet = this.getHelmet(false);
        if (helmet == null || helmet.isEmpty()) {
            equipment.removeKey("head");
        } else {
            for (ItemStack itemStack : helmet) {
                if (itemStack != null) {
                    equipment.setItemStack("head", itemStack);
                    break;
                }
            }
        }

        List<ItemStack> chestplate = this.getChestplate(false);
        if (chestplate == null || chestplate.isEmpty()) {
            equipment.removeKey("chest");

        } else {
            for (ItemStack itemStack : chestplate) {
                if(itemStack != null) {
                    equipment.setItemStack("chest", itemStack);
                    break;
                }
            }
        }
        List<ItemStack> leggings = this.getLeggings(false);
        if (leggings == null || leggings.isEmpty()) {
            equipment.removeKey("legs");

        } else {
            for (ItemStack itemStack : leggings) {
                if (itemStack != null) {
                    equipment.setItemStack("legs", itemStack);
                    break;
                }
            }
        }
        List<ItemStack> boots = this.getBoots(false);
        if (boots == null || boots.isEmpty()) {

            equipment.removeKey("feet");
        } else {
            for (ItemStack itemStack : boots) {
                if (itemStack != null) {
                    equipment.setItemStack("feet", itemStack);
                    break;
                }
            }
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

            cache.setTotalExperience(player.getTotalExperience());
            cache.setExp(player.getExp());
            cache.setLevel(player.getLevel());

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

            cache.setLevel(data.getInteger("XpLevel"));
            cache.setTotalExperience(data.getInteger("XpTotal"));
            cache.setExp( data.getFloat("XpP"));
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
        @Setter
        private int totalExperience = 0;

        @Getter
        @Setter
        private float exp = 0.0f;

        @Getter
        @Setter
        private int level = 0;

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
