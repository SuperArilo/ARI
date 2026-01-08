package com.tty.commands;

import com.tty.commands.sub.EnderChestToPlayer;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class enderchest extends BaseLiteralArgumentLiteralCommand {

    public enderchest() {
        super(false, 1, true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnderChestToPlayer());
    }

    @Override
    public String name() {
        return "enderchest";
    }

    @Override
    public String permission() {
        return "ari.command.enderchest";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.openInventory(player.getEnderChest());
    }
}
