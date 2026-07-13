package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "infinitytotem", permission = "ari.command.infinitytotem", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class infinitytotem extends LiteralArgumentCommand {

    public static final List<Player> INFINITY_TOTEM_PLAYER_LIST = new ArrayList<>();

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (INFINITY_TOTEM_PLAYER_LIST.remove(player)) {
            ConfigUtils.t("function.infinitytotem.disable", player).thenAccept(player::sendMessage);
        } else {
            INFINITY_TOTEM_PLAYER_LIST.add(player);
            ConfigUtils.t("function.infinitytotem.enable", player).thenAccept(player::sendMessage);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
