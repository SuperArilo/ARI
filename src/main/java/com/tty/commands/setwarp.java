package com.tty.commands;

import com.tty.commands.args.SetWarpArgs;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "setwarp", permission = "ari.command.setwarp", tokenLength = 2)
@LiteralCommand
public class setwarp extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetWarpArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
