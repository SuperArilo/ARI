package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.tpa.TpaAcceptArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.enumType.FilePath;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tpaaccept", permission = "ari.command.tpaaccept", tokenLength = 2)
@LiteralCommand
public class tpaaccept extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaAcceptArgs());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.instance.getConfigInstance().getObject(FilePath.TPA_CONFIG.name()));
    }

}
