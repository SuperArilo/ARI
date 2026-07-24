package com.tty.ari.commands.sub.playername.suffix;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.playername.SetSuffixArgs;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "set", permission = "ari.command.playername", tokenLength = 4, allowConsole = true)
@LiteralCommand
public class SetSuffix extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetSuffixArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
