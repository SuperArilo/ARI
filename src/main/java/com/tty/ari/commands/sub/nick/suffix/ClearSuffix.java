package com.tty.ari.commands.sub.nick.suffix;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.ari.commands.sub.nick.PublicNickClear;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import org.bukkit.command.CommandSender;

@CommandMeta(displayName = "clear", permission = "ari.command.nick", tokenLength = 4, allowConsole = true)
@LiteralCommand(directExecute = true)
public class ClearSuffix extends PublicNickClear {

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.setValue(sender, args, PlaceholderPlayer.PLAYER_NAME_SUFFIX);
    }

}
