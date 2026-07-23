package com.tty.ari.commands.sub.playername;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.playername.NamePrefixArgs;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "prefix", permission = "ari.command.playername", tokenLength = 3, allowConsole = true)
@LiteralCommand
public class NamePrefix extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new NamePrefixArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
