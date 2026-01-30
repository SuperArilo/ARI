package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoAddArgs;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "add", permission = "ari.command.zako.add", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoAdd extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoAddArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
