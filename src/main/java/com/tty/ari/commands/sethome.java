package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.SetHomeArgs;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.enumType.TeleportType;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "sethome", permission = "ari.command.sethome", tokenLength = 2)
@LiteralCommand
public class sethome extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetHomeArgs());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(FunctionConfig.class).isEnable(TeleportType.HOME);
    }

}
