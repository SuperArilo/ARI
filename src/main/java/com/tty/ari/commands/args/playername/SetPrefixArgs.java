package com.tty.ari.commands.args.playername;

import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import org.bukkit.command.CommandSender;

@CommandMeta(displayName = "content (string)", permission = "ari.command.playername", tokenLength = 5, allowConsole = true)
@ArgumentCommand
public class SetPrefixArgs extends PublicPlayerNameSetArgs {

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.setValue(sender, args, PlaceholderPlayer.PLAYER_NAME_PREFIX);
    }

}
