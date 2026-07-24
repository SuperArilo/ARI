package com.tty.ari.commands.args.nick;

import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import org.bukkit.command.CommandSender;

@CommandMeta(displayName = "content (string)", permission = "ari.command.nick", tokenLength = 5, allowConsole = true)
@ArgumentCommand
public class SetPrefixArgs extends PublicNickSetArgs {

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.setValue(sender, args, PlaceholderPlayer.PLAYER_NAME_PREFIX);
    }

}
