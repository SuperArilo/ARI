package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoRemoveArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "remove", permission = "ari.command.zako.remove", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoRemove extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoRemoveArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
