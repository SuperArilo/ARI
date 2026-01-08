package com.tty.commands.args.enchant;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EnchantForceEnchant extends EnchantBaseArgs<Boolean> {

    protected EnchantForceEnchant() {
        super(4, BoolArgumentType.bool(), false);
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
    public String name() {
        return "force enchant (boolean)";
    }

    @Override
    public String permission() {
        return "ari.command.enchant";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ResultArgs resultArgs = this.parseArgs(sender, args);
        Player player = (Player) sender;
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        this.enchant(sender, itemInMainHand, resultArgs);
    }
}
