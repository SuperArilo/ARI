package com.tty.commands;

import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.SetWarpArgs;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.enumType.FilePath;
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
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    protected boolean isDisabledInGame() {
        return this.getDisableStatus(Ari.C_INSTANCE.getObject(FilePath.WARP_CONFIG.name()));
    }

}
