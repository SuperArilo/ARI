package com.tty.ari.commands.sub.zako;

import com.mojang.brigadier.Command;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.zako.ban.ZakoBanPlayer;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "ban", permission = "ari.command.zako.ban", tokenLength = 2, allowConsole = true)
@LiteralCommand
public class ZakoBan extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanPlayer());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
