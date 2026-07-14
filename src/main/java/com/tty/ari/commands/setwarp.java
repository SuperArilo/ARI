package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.SetWarpArgs;
import com.tty.ari.configuration.warp.WarpConfig;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(WarpConfig.class).isEnable();
    }

}
