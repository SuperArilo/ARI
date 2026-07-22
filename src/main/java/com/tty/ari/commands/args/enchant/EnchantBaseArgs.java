package com.tty.ari.commands.args.enchant;

import com.tty.api.ComponentTool;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.enumType.lang.PlaceholderEnchant;
import com.tty.ari.tool.ConfigUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class EnchantBaseArgs <T> extends RequiredArgumentCommand<T> {

    @Data
    protected static class ResultArgs {
        private Enchantment enchantment;
        private final int level;
        private final boolean forceEnchant;
        private final boolean forceLevel;
        private final boolean unbreakable;

        public ResultArgs(Enchantment enchantment, int level, boolean forceEnchant, boolean forceLevel, boolean unbreakable) {
            this.enchantment = enchantment;
            this.level = level;
            this.forceEnchant = forceEnchant;
            this.forceLevel = forceLevel;
            this.unbreakable = unbreakable;
        }
    }

    protected ResultArgs parseArgs(CommandSender sender, String[] args) {
        if (args.length < 3) return null;
        String enchantArg = args[1];
        Enchantment enchantParse = this.parseEnchant(enchantArg);
        if (enchantParse == null) {
            ConfigUtils.t("function.enchant.not-exist", (Player) sender).thenAccept(sender::sendMessage);
            return null;
        }
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-edit.number.format-error")));
            level = 0;
        }
        boolean forceEnchant = false;
        if (args.length >= 4) {
            forceEnchant = Boolean.parseBoolean(args[3]);
        }
        boolean forceLevel = false;
        if (args.length >= 5) {
            forceLevel = Boolean.parseBoolean(args[4]);
        }
        boolean unbreakable = false;
        if (args.length == 6) {
            unbreakable = Boolean.parseBoolean(args[5]);
        }
        return new ResultArgs(enchantParse, level, forceEnchant, forceLevel, unbreakable);
    }

    /**
     * 根据 附魔名称返回指定的 Enchantment对象
     * @param enchant 附魔名称 key
     * @return Enchantment 对象
     */
    protected @Nullable Enchantment parseEnchant(String enchant) {
        NamespacedKey key = enchant.contains(":")
                ? NamespacedKey.fromString(enchant)
                : NamespacedKey.minecraft(enchant);
        if (key == null) return null;
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
    }

    protected void enchant(CommandSender sender, @NotNull ItemStack itemStack, @NotNull ResultArgs args) {
        Enchantment enchantment = args.getEnchantment();
        int level = args.getLevel();
        boolean forceEnchant = args.isForceEnchant();
        boolean forceLevel = args.isForceLevel();
        Player player = (Player) sender;

        if (!enchantment.canEnchantItem(itemStack) && !forceEnchant) {
            ConfigUtils.t("function.enchant.can-not-apply-item", player).thenAccept(sender::sendMessage);
            return;
        }

        if (!forceLevel && level > enchantment.getMaxLevel()) {
            ConfigUtils.t("function.enchant.level-too-high", player).thenAccept(sender::sendMessage);
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            itemMeta = Bukkit.getServer().getItemFactory().getItemMeta(itemStack.getType());
            if (itemMeta == null) {
                ConfigUtils.t("function.enchant.can-not-apply-item", player).thenAccept(sender::sendMessage);
                return;
            }
        }
        itemMeta.addEnchant(enchantment, level, forceLevel);
        if (!itemMeta.isUnbreakable()) {
            itemMeta.setUnbreakable(args.isUnbreakable());
        }
        itemStack.setItemMeta(itemMeta);

        String value = Ari.DATA_SERVICE.getValue("enchantment." + enchantment.key().value());
        sender.sendMessage(ConfigUtils.tAfter("function.enchant.enchant-success", Map.of(PlaceholderEnchant.ENCHANT_NAME_UNRESOLVED.getType(), Component.text(value), PlaceholderEnchant.ENCHANT_LEVEL_UNRESOLVED.getType(), Component.text(level))));
    }
}
