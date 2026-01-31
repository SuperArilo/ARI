package com.tty.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.command.RequiredArgumentCommand;
import com.tty.enumType.FilePath;
import com.tty.enumType.lang.LangTime;
import com.tty.function.TimeManager;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.enumType.TimePeriod;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.tty.listener.player.PlayerSkipNight.isBedWorksRe;

@CommandMeta(displayName = "period (string)", permission = "ari.command.time", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class TimeArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> strings = new HashSet<>();
        for (TimePeriod value : TimePeriod.values()) {
            strings.add(value.getDescription());
        }
        return CompletableFuture.completedFuture(strings);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String timePeriod = args[1];
        TimePeriod period;
        try {
            period = TimePeriod.valueOf(timePeriod.toUpperCase());
        } catch (Exception e) {
            player.sendMessage(ConfigUtils.tAfter("server.time.not-exist-period", Map.of(LangTime.TIME_PERIOD_UNRESOLVED.getType(), Component.text(timePeriod))));
            return;
        }
        World world = player.getWorld();
        if (!isBedWorksRe(world)) {
            ConfigUtils.t("server.time.not-allowed-world", player).thenAccept(player::sendMessage);
            return;
        }
        TimeManager.build(world).timeSet(period.getStart());
        String value = Ari.C_INSTANCE.getValue("server.time.tips", FilePath.LANG);
        if (value == null) {
            player.sendMessage("no content " + timePeriod + "in lang");
            return;
        }
        player.sendMessage(Ari.COMPONENT_SERVICE.text(value, Map.of(LangTime.EXECUTE_TARGET_TIME.getType(), ConfigUtils.tAfter("server.time.name." + period.getDescription()))));
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
