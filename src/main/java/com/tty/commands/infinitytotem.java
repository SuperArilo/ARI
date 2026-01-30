package com.tty.commands;

import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.command.LiteralArgumentCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandMeta(displayName = "infinitytotem", permission = "ari.command.infinitytotem", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class infinitytotem extends LiteralArgumentCommand {

    public static final List<Player> INFINITY_TOTEM_PLAYER_LIST = new ArrayList<>();

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (INFINITY_TOTEM_PLAYER_LIST.remove(player)) {
            ConfigUtils.t("function.infinitytotem.disable", player).thenAccept(player::sendMessage);
        } else {
            INFINITY_TOTEM_PLAYER_LIST.add(player);
            ConfigUtils.t("function.infinitytotem.enable", player).thenAccept(player::sendMessage);
        }
    }

}
