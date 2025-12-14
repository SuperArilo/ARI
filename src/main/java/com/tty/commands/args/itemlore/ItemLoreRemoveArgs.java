package com.tty.commands.args.itemlore;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.Ari;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemLoreRemoveArgs extends BaseRequiredArgumentLiteralCommand<Integer> {

    public ItemLoreRemoveArgs() {
        super(false, 3, IntegerArgumentType.integer(), false);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "line (int)";
    }

    @Override
    public String permission() {
        return "ari.command.itemlore.remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String content = args[2];
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) {
            player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-player.hand-no-item")));
            return;
        }
        ItemMeta itemMeta = mainHand.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        try {
            int index = Integer.parseInt(content) - 1;
            if (index < 0) {
                return;
            }
            lore.remove(index);
        } catch (Exception e) {
            player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.input-error")));
        }
        itemMeta.lore(lore);
        mainHand.setItemMeta(itemMeta);
    }
}
