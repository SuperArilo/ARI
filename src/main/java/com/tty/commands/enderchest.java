package com.tty.commands;

import com.tty.commands.sub.EnderChestToPlayer;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "enderchest", permission = "ari.command.enderchest", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class enderchest extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnderChestToPlayer());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.openInventory(player.getEnderChest());
    }
}
