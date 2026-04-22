package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.SetWarpArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.enumType.FilePath;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "setwarp", permission = "ari.command.setwarp", tokenLength = 2)
@LiteralCommand
public class setwarp extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new SetWarpArgs());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.instance.getConfigInstance().getObject(FilePath.WARP_CONFIG.name()));
    }

}
