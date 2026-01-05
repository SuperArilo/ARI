package com.tty.commands.args;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ItemNameArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public ItemNameArgs() {
        super(2, StringArgumentType.string());
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(Set.of());
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "\"content\" (string)";
    }

    @Override
    public String permission() {
        return "ari.command.itemname";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) {
            player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-player.hand-no-item")));
            return;
        }
        ItemMeta itemMeta = mainHand.getItemMeta();
        itemMeta.displayName(ComponentUtils.text(args[1]));
        mainHand.setItemMeta(itemMeta);
    }
}
