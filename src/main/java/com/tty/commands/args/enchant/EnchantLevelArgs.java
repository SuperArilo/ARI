package com.tty.commands.args.enchant;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "enchant", permission = "ari.command.enchant", tokenLength = 3)
@ArgumentCommand(isSuggests = true)
public class EnchantLevelArgs extends EnchantBaseArgs<Integer> {

    @Override
    protected @NotNull ArgumentType<Integer> argumentType() {
        return IntegerArgumentType.integer(0);
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        String enchant = args[1];
        HashSet<String> hashSet = new HashSet<>();
        Enchantment parse = this.parseEnchant(enchant);
        if (parse == null) return CompletableFuture.completedFuture(Set.of("unable enchant"));
        for(int i = parse.getStartLevel();i <= parse.getMaxLevel();i++) {
            hashSet.add(String.valueOf(i));
        }
        return CompletableFuture.completedFuture(hashSet);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantForceEnchant());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        EnchantBaseArgs.ResultArgs resultArgs = this.parseArgs(sender, args);
        if (resultArgs == null) return;
        Player player = (Player) sender;
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        this.enchant(sender, itemInMainHand, resultArgs);
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
