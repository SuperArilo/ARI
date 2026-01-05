package com.tty.commands.args;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.function.TimeManager;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.enum_type.TimePeriod;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.tty.listener.player.PlayerSkipNight.isBedWorksRe;

public class TimeArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public TimeArgs() {
        super(false, 2, StringArgumentType.string(), true);
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
    public String name() {
        return "period (string)";
    }

    @Override
    public String permission() {
        return "ari.command.time";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String timePeriod = args[1];
        TimePeriod period;
        try {
            period = TimePeriod.valueOf(timePeriod.toUpperCase());
        } catch (Exception e) {
            player.sendMessage(ConfigUtils.t("server.time.not-exist-period", Map.of(LangType.PERIOD.getType(), Component.text(timePeriod))));
            return;
        }
        World world = player.getWorld();
        if (!isBedWorksRe(world)) {
            player.sendMessage(ConfigUtils.t("server.time.not-allowed-world"));
            return;
        }
        TimeManager.build(world).timeSet(period.getStart());
        String value = Ari.C_INSTANCE.getValue("server.time.tips", FilePath.LANG);
        if (value == null) {
            player.sendMessage("no content " + timePeriod + "in lang");
            return;
        }
        player.sendMessage(ComponentUtils.text(value, Map.of(LangType.TIME.getType(), ConfigUtils.t("server.time.name." + period.getDescription()))));
    }
}
