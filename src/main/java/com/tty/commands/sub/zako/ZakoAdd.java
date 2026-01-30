package com.tty.commands.sub.zako;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.zako.ZakoAddArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "add", permission = "ari.command.zako.add", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoAdd extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoAddArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
