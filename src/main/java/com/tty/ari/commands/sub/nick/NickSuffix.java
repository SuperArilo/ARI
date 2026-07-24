package com.tty.ari.commands.sub.nick;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.sub.nick.suffix.ClearSuffix;
import com.tty.ari.commands.sub.nick.suffix.SetSuffix;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "suffix", permission = "ari.command.nick", tokenLength = 3, allowConsole = true)
@LiteralCommand
public class NickSuffix extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetSuffix(), new ClearSuffix());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
