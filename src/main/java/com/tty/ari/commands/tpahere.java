package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.tpa.TpaHereArgs;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.enumType.TeleportType;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "tpahere", permission = "ari.command.tpahere", tokenLength = 2)
@LiteralCommand
public class tpahere extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new TpaHereArgs());
    }

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(FunctionConfig.class).isEnable(TeleportType.TPA);
    }

}
