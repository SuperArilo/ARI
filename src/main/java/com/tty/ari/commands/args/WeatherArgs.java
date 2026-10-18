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
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "weather", permission = "ari.command.weather", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class WeatherArgs extends RequiredArgumentCommand<String> {

    private static final Set<String> WEATHER_TYPES = Set.of("clear", "rain", "thunder");

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return CompletableFuture.completedFuture(new HashSet<>(WEATHER_TYPES));
        } else {
            return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], WEATHER_TYPES));
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        World world;
        if (sender instanceof Player player) {
            world = player.getWorld();
        } else {
            world = Ari.instance.getServer().getWorlds().isEmpty()
                    ? null
                    : Ari.instance.getServer().getWorlds().getFirst();
        }
        if (world == null) return;

        String type = args[1].toLowerCase();
        switch (type) {
            case "clear" -> {
                world.setStorm(false);
                world.setThundering(false);
            }
            case "rain" -> {
                world.setStorm(true);
                world.setThundering(false);
            }
            case "thunder" -> {
                world.setStorm(true);
                world.setThundering(true);
            }
            default -> {
                sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-edit.input-error"), sender instanceof Player ? (Player) sender : null));
                return;
            }
        }
        ConfigUtils.t("function.weather.changed", sender instanceof Player ? (Player) sender : null).thenAccept(sender::sendMessage);
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
