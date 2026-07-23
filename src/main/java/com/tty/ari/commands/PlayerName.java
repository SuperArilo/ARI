package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.playername.PlayerNameList;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "playername", permission = "ari.command.playername", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class PlayerName extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new PlayerNameList());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
