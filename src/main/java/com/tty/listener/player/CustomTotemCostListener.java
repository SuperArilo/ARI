package com.tty.listener.player;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.commands.infinitytotem;
import com.tty.dto.event.CustomPluginReloadEvent;
import com.tty.enumType.FilePath;
import com.tty.lib.Log;
import com.tty.lib.tool.PermissionUtils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomTotemCostListener implements Listener {

    private boolean enable;
    private List<String> disableWorlds = new ArrayList<>();

    public CustomTotemCostListener() {
        this.enable = this.isEnable();
        this.disableWorlds = this.getDisableWorlds();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!this.enable) return;
        if (this.disableWorlds.contains(player.getWorld().getName())) return;
        if (!PermissionUtils.hasPermission(player, "ari.totem.inventory-trigger")) return;

        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage > 0) return;
        if (this.hasHandTotem(player)) return;

        if (!this.hasTotemToUse(player)) return;
        event.setCancelled(true);
        this.resurrectPlayer(player);
        this.awardAdvancement(player);
    }

    @EventHandler
    public void onPluginReload(CustomPluginReloadEvent event) {
        this.enable = this.isEnable();
        this.disableWorlds = this.getDisableWorlds();
    }

    /**
     * 判断玩家的主副手是否存在图腾，如果有就走原版的处理流程
     * @param player 被检查的玩家
     * @return 布尔值
     */
    private boolean hasHandTotem(Player player) {
        PlayerInventory inventory = player.getInventory();
        return inventory.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                || inventory.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;
    }

    /**
     * 搜索玩家的背包里是否存在不死图腾
     * @param player 被搜索的玩家
     * @return 有 true 无 false
     */
    private boolean hasTotemToUse(Player player) {
        if (infinitytotem.INFINITY_TOTEM_PLAYER_LIST.contains(player)) return true;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize();i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.isEmpty()) continue;
            if (item.getType().equals(Material.TOTEM_OF_UNDYING)) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    inventory.setItem(i, null);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 手动复活玩家
     * @param player 被复活的玩家
     */
    private void resurrectPlayer(Player player) {
        player.setHealth(1.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));
        player.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
    }

    /**
     * 手动完成玩家的不死图腾成就
     * @param player 被执行的玩家
     */
    private void awardAdvancement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("adventure/totem_of_undying"));
        if (advancement == null) {
            Log.error("not found advancement adventure/totem_of_undying. skipping...");
            return;
        }
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) {
            Log.debug("advancement adventure/totem_of_undying for player {} is done.", player.getName());
            return;
        }
        Collection<String> awardedCriteria = progress.getRemainingCriteria();
        for (String awardedCriterion : awardedCriteria) {
            progress.awardCriteria(awardedCriterion);
        }
    }

    private boolean isEnable() {
        return Ari.C_INSTANCE.getValue("totem.allow-inventory-trigger", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

    private List<String> getDisableWorlds() {
        return Ari.C_INSTANCE.getValue("totem.disable-world", FilePath.FUNCTION_CONFIG, new TypeToken<List<String>>(){}.getType(), List.of());
    }

}
