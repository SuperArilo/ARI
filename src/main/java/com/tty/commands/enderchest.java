package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.sub.EnderChestToPlayer;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "enderchest", permission = "ari.command.enderchest", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class enderchest extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnderChestToPlayer());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.openInventory(player.getEnderChest());
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
