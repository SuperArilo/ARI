package com.tty.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.lib.command.BaseCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class itemname extends BaseCommand<String> {

    public itemname() {
        super(false, StringArgumentType.string(), 2);
    }

    @Override
    public List<SuperHandsomeCommand> getSubCommands() {
        return List.of();
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of("<\"name\" (string)>");
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

    @Override
    public String name() {
        return "itemname";
    }

    @Override
    public String permission() {
        return "ari.command.itemname";
    }
}
