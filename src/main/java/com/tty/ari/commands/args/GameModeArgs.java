package com.tty.ari.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "gamemode", permission = "ari.command.gamemode", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class GameModeArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {

        Set<String> objects = new HashSet<>();
        for (GameMode value : GameMode.values()) {
            objects.add(value.name().toLowerCase());
        }

        CompletableFuture<Set<String>> future = new CompletableFuture<>();
        if (args.length == 1) {
            future.complete(objects);
        } else {
            future.complete(PublicFunctionUtils.tabList(args[1], objects));
        }
        return future;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        GameMode mode;
        try {
            mode = GameMode.valueOf(args[1].toUpperCase());
        } catch (Exception e){
            sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-edit.input-error"), player));
            return;
        }
        player.setGameMode(mode);
        ConfigUtils.t("function.gamemode.changed", player).thenAccept(sender::sendMessage);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
