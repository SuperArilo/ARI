package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.nick.PlayerNickList;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "nick", permission = "ari.command.nick", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class Nick extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new PlayerNickList());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
