package com.tty.commands;

import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class infinitytotem extends BaseLiteralArgumentLiteralCommand {

    public static final List<Player> INFINITY_TOTEM_PLAYER_LIST = new ArrayList<>();

    public infinitytotem() {
        super(false, 1, true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "infinitytotem";
    }

    @Override
    public String permission() {
        return "ari.command.infinitytotem";
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
