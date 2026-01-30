package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoInfoArgs;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.*;

@CommandMeta(displayName = "info", permission = "ari.command.zako.info", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoInfo extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoInfoArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
