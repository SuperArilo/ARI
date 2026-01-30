package com.tty.commands.args.itemlore;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.command.RequiredArgumentCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "\"content\" (string)", permission = "ari.command.itemlore.add", tokenLength = 3)
@ArgumentCommand
public class ItemloreAddArgs extends RequiredArgumentCommand<String> {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) {
            player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-player.hand-no-item")));
            return;
        }
        ItemMeta itemMeta = mainHand.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(Ari.COMPONENT_SERVICE.text(args[2]));
        itemMeta.lore(lore);
        mainHand.setItemMeta(itemMeta);
    }
}
