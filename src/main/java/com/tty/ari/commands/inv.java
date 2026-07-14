package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.InventoryCheck;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "inv", permission = "ari.command.inv", tokenLength = 2)
@LiteralCommand
public class inv extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new InventoryCheck());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
