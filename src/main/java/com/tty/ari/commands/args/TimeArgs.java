package com.tty.ari.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.enumType.TimePeriod;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.configuration.lang.LangConfig;
import com.tty.ari.enumType.lang.PlaceholderTime;
import com.tty.ari.function.TimeManager;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.tty.ari.listener.player.PlayerSkipNight.isBedWorksRe;

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
        if (args.length == 1) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], strings));
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public CompletableFuture<Void> execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String timePeriod = args[1];
        TimePeriod period;
        try {
            period = TimePeriod.valueOf(timePeriod.toUpperCase());
        } catch (Exception e) {
            player.sendMessage(ConfigUtils.tAfter("server.time.not-exist-period", Map.of(PlaceholderTime.TIME_PERIOD_UNRESOLVED.getType(), Component.text(timePeriod))));
            return CompletableFuture.completedFuture(null);
        }
        World world = player.getWorld();
        if (!isBedWorksRe(world)) {
            return ConfigUtils.t("server.time.not-allowed-world", player).thenAccept(player::sendMessage);
        }
        TimeManager.build(world).timeSet(period.getStart());
        String value = Ari.instance.getConfigurationManager().get(LangConfig.class).getString("server.time.tips");
        if (value == null) {
            player.sendMessage("no content " + timePeriod + "in lang");
            return CompletableFuture.completedFuture(null);
        }
        player.sendMessage(Ari.instance.getComponentTool().text(value, Map.of(PlaceholderTime.EXECUTE_TARGET_TIME.getType(), ConfigUtils.tAfter("server.time.name." + period.getDescription()))));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
