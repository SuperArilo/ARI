package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ban.ZakoBanPlayer;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "ban", permission = "ari.command.zako.ban", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoBan extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanPlayer());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
