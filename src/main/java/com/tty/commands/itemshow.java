package com.tty.commands;

import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class itemshow extends BaseLiteralArgumentLiteralCommand {

    public itemshow() {
        super(false, 1, true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "itemshow";
    }

    @Override
    public String permission() {
        return "ari.command.itemshow";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            ConfigUtils.t("function.itemshow.no-item-in-hand", player).thenAccept(t ->
                    Lib.Scheduler.run(Ari.instance, i -> sender.sendMessage(t)));
            return;
        }
        ConfigUtils.t("function.itemshow.show-to-players", player).thenAccept(t ->
                Lib.Scheduler.run(Ari.instance, i -> Bukkit.broadcast(t)));
    }

}
