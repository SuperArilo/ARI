package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.TeleportToPlayerArgs;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tp", permission = "ari.command.tp", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class TeleportToPlayer extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TeleportToPlayerArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
