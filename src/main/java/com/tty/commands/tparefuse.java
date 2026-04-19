package com.tty.commands;

import com.mojang.brigadier.Command;
import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.tpa.TpaRefuseArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.enumType.FilePath;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "tparefuse", permission = "ari.command.tparefuse", tokenLength = 2)
@LiteralCommand
public class tparefuse extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaRefuseArgs());
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
