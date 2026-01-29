package com.tty.commands.args.enchant;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.Ari;
import com.tty.enumType.lang.LangEnchant;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.tool.ConfigUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class EnchantBaseArgs <T> extends BaseRequiredArgumentLiteralCommand<T> {

    protected EnchantBaseArgs(ArgumentType<T> type) {
        super(type);
    }

    @Data
    protected static class ResultArgs {
        private Enchantment enchantment;
        private int level;
        private boolean forceEnchant;
        private boolean forceLevel;

        public ResultArgs(Enchantment enchantment, int level, boolean forceEnchant, boolean forceLevel) {
            this.enchantment = enchantment;
            this.level = level;
            this.forceEnchant = forceEnchant;
            this.forceLevel = forceLevel;
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
        int level = Integer.parseInt(args[2]);
        boolean forceEnchant = false;
        if (args.length >= 4) {
            forceEnchant = Boolean.parseBoolean(args[3]);
        }
        boolean forceLevel = false;
        if (args.length == 5) {
            forceLevel = Boolean.parseBoolean(args[4]);
        }
        return new ResultArgs(enchantParse, level, forceEnchant, forceLevel);
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
        itemMeta.addEnchant(enchantment, level, forceLevel);
        itemStack.setItemMeta(itemMeta);

        String value = Ari.DATA_SERVICE.getValue("enchantment." + enchantment.key().value());
        sender.sendMessage(ConfigUtils.tAfter("function.enchant.enchant-success",
                Map.of(LangEnchant.ENCHANT_NAME_UNRESOLVED.getType(), Component.text(value), LangEnchant.ENCHANT_LEVEL_UNRESOLVED.getType(), Component.text(level))));
    }
}
