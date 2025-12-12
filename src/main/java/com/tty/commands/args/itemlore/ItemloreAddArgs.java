package com.tty.commands.args.itemlore;

import com.mojang.brigadier.arguments.StringArgumentType;
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

public class ItemloreAddArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public ItemloreAddArgs() {
        super(StringArgumentType.string(), false);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public List<String> tabSuggestions() {
        return List.of();
    }

    @Override
    public String name() {
        return "<\"content\" (string)>";
    }

    @Override
    public String permission() {
        return "ari.command.itemlore.add";
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
        List<Component> lore = itemMeta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(ComponentUtils.text(args[2]));
        mainHand.setItemMeta(itemMeta);
    }
}
