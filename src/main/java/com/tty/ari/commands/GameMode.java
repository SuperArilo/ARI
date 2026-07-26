package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.GameModeArgs;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "gamemode", permission = "ari.command.gamemode", tokenLength = 1)
@LiteralCommand(directExecute = true)

public class GameMode extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new GameModeArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
