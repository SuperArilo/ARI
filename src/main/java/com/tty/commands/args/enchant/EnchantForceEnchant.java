package com.tty.commands.args.enchant;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "force enchant (boolean)", permission = "ari.command.enchant", tokenLength = 4)
@ArgumentCommand
public class EnchantForceEnchant extends EnchantBaseArgs<Boolean> {

    @Override
    protected @NotNull ArgumentType<Boolean> argumentType() {
        return BoolArgumentType.bool();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantForceLevel());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ResultArgs resultArgs = this.parseArgs(sender, args);
        Player player = (Player) sender;
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        this.enchant(sender, itemInMainHand, resultArgs);
    }
}
