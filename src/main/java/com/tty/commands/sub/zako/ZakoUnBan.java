package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoUnBanPlayerArgs;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "unban", permission = "ari.command.zako.unban", allowConsole = true, tokenLength = 2)
@LiteralCommand
public class ZakoUnBan extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoUnBanPlayerArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
