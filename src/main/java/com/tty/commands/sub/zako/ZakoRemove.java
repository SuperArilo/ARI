package com.tty.commands.sub.zako;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.zako.ZakoRemoveArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "remove", permission = "ari.command.zako.remove", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoRemove extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoRemoveArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
