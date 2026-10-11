package com.tty.ari.commands.args.itemlore;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "line numbers (int...)", permission = "ari.command.itemlore.remove", tokenLength = 3)
@ArgumentCommand(isSuggests = true)
public class ItemLoreRemoveArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) {
            return CompletableFuture.completedFuture(Set.of());
        }
        ItemMeta itemMeta = mainHand.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if (lore == null || lore.isEmpty()) {
            return CompletableFuture.completedFuture(Set.of());
        }
        Set<String> suggestions = new LinkedHashSet<>();
        for (int i = 0; i < lore.size(); i++) {
            suggestions.add(String.valueOf(i + 1));
        }
        return CompletableFuture.completedFuture(suggestions);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String content = args[2];
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) {
            player.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-player.hand-no-item"), player));
            return;
        }
        ItemMeta itemMeta = mainHand.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        Set<Integer> indicesToRemove = new LinkedHashSet<>();
        boolean hasError = false;
        for (String part : content.trim().split("\\s+")) {
            int idx = parseLineIndex(part);
            if (idx < 0) {
                hasError = true;
            } else if (idx >= lore.size()) {
                hasError = true;
            } else {
                indicesToRemove.add(idx);
            }
        }
        if (hasError) {
            player.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-edit.input-error"), player));
            return;
        }
        if (indicesToRemove.isEmpty()) {
            return;
        }
        List<Integer> sortedIndices = new ArrayList<>(indicesToRemove);
        sortedIndices.sort(Comparator.reverseOrder());
        for (int idx : sortedIndices) {
            if (idx < lore.size()) {
                lore.remove(idx);
            }
        }
        itemMeta.lore(lore);
        mainHand.setItemMeta(itemMeta);
        player.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.command.execute-success"), player));
    }

    private int parseLineIndex(String part) {
        int colonIdx = part.indexOf(':');
        String numPart;
        if (colonIdx > 0) {
            numPart = part.substring(0, colonIdx).trim();
        } else {
            numPart = part.trim();
        }
        try {
            int n = Integer.parseInt(numPart);
            return n - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }
}
