package com.tty.commands.args.enchant;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "force level (boolean)", permission = "ari.command.enchant", tokenLength = 5)
@ArgumentCommand
public class EnchantForceLevel extends EnchantBaseArgs<Boolean> {

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
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        EnchantBaseArgs.ResultArgs resultArgs = this.parseArgs(sender, args);
        Player player = (Player) sender;
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        this.enchant(sender, itemInMainHand, resultArgs);
    }
}
