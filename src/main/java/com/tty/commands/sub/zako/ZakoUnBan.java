package com.tty.commands.sub.zako;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.zako.ZakoUnBanPlayerArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "unban", permission = "ari.command.zako.unban", allowConsole = true, tokenLength = 2)
@LiteralCommand
public class ZakoUnBan extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoUnBanPlayerArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
