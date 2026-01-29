package com.tty.commands.args;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
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

@CommandMeta(displayName = "\"content\" (string)", permission = "ari.command.itemname", tokenLength = 2)
@ArgumentCommand
public class ItemNameArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public ItemNameArgs() {
        super(StringArgumentType.string());
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
        Player player = (Player) sender;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) {
            player.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.hand-no-item")));
            return;
        }
        ItemMeta itemMeta = mainHand.getItemMeta();
        itemMeta.displayName(ComponentUtils.text(args[1]));
        mainHand.setItemMeta(itemMeta);
    }
}
