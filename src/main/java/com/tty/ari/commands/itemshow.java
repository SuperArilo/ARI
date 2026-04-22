package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@CommandMeta(displayName = "itemshow", permission = "ari.command.itemshow", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class itemshow extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            ConfigUtils.t("function.itemshow.no-item-in-hand", player).thenAccept(t ->
                    Ari.instance.getScheduler().run(Ari.instance, i -> sender.sendMessage(t)));
            return 0;
        }
        ConfigUtils.t("function.itemshow.show-to-players", player).thenAccept(t ->
                Ari.instance.getScheduler().run(Ari.instance, i -> Bukkit.broadcast(t)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
