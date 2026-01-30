package com.tty.commands.sub.zako;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.zako.ZakoInfoArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.*;

@CommandMeta(displayName = "info", permission = "ari.command.zako.info", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoInfo extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoInfoArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
