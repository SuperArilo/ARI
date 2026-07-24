package com.tty.ari.commands.sub.playername.suffix;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.ari.commands.sub.playername.PublicNameClear;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import org.bukkit.command.CommandSender;

@CommandMeta(displayName = "clear", permission = "ari.command.playername", tokenLength = 4, allowConsole = true)
@LiteralCommand(directExecute = true)
public class ClearSuffix extends PublicNameClear {

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.setValue(sender, args, PlaceholderPlayer.PLAYER_NAME_SUFFIX);
    }

}
