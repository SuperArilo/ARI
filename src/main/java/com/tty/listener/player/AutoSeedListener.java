package com.tty.listener.player;

import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.lib.tool.PublicFunctionUtils;
import net.kyori.adventure.sound.Sound;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

public class AutoSeedListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!Ari.instance.getConfig().getBoolean("server.auto-seed", false)) return;
        if (!event.getAction().isRightClick()) return;
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!mainHand.getType().name().endsWith("_HOE")) return; // 检查锄头

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        BlockData blockData = clickedBlock.getBlockData();
        if (!(blockData instanceof Ageable ageable)) return;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        Material seedType = getSeedForCrop(blockData.getMaterial());
        if (offHand.getType() != seedType || offHand.getAmount() <= 0) return; // 副手检查

        if (ageable.getAge() < ageable.getMaximumAge()) return; // 只处理成熟作物


        ItemMeta meta = mainHand.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;
        if (damageable.getDamage() >= mainHand.getType().getMaxDurability()) return;


        Lib.Scheduler.runAtEntity(Ari.instance, player, i -> {

            // 随机消耗耐久
            if(PublicFunctionUtils.randomGenerator(0, 1) == 0 && !player.getGameMode().equals(GameMode.CREATIVE)) {
                damageable.setDamage(damageable.getDamage() + 1);
                mainHand.setItemMeta(meta);
            }

            int level = mainHand.getEnchantmentLevel(Enchantment.FORTUNE);

            // 掉落作物本身
            Material cropMaterial = getCropBlock(blockData.getMaterial());
            int cropDropAmount = PublicFunctionUtils.randomGenerator(1, level + 2);
            clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation(), new ItemStack(cropMaterial, cropDropAmount));

            // 掉落种子或副手可种植物品
            int seedDropAmount = this.getSeedDropAmount(seedType, level);
            if (seedDropAmount > 0) {
                clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation(), new ItemStack(seedType, seedDropAmount));
            }

            // 自动播种
            Ageable newAgeable = (Ageable) clickedBlock.getBlockData();
            newAgeable.setAge(0);
            clickedBlock.setBlockData(newAgeable);

            // 消耗副手种子
            offHand.setAmount(offHand.getAmount() - 1);
            player.playSound(Sound.sound(ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f));
        }, null);

        event.setCancelled(true);
    }

    // 返回副手种子类型
    private Material getSeedForCrop(Material type) {
        return switch (type) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            default -> Material.AIR;
        };
    }

    // 返回作物本身
    private Material getCropBlock(Material type) {
        return switch (type) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            default -> Material.AIR;
        };
    }

    // 种子掉落数量
    private int getSeedDropAmount(Material seed, int fortuneLevel) {
        if (seed == Material.WHEAT_SEEDS || seed == Material.BEETROOT_SEEDS) {
            return PublicFunctionUtils.randomGenerator(fortuneLevel >= 1 ? 2 : 1, fortuneLevel + 2);
        }
        return 0;
    }
}
