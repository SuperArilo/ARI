package com.tty.commands.sub.zako;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.function.PlayerManager;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.services.ConfigDataService;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZakoInfo extends ZakoBase<String> {

    public ZakoInfo(boolean allowConsole, ArgumentType<String> type) {
        super(allowConsole, type, 3);
    }

    @Override
    public List<SuperHandsomeCommand> getSubCommands() {
        return List.of();
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of("<name or uuid (string)>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String value = args[2];
        UUID uuid = this.parseUUID(value);
        if (uuid == null) return;

        PlayerManager manager = new PlayerManager(true);
        manager.getInstance(uuid.toString())
            .thenAccept(instance -> {
                if(instance == null) {
                    sender.sendMessage(ConfigUtils.t("function.zako.not-exist"));
                    return;
                }
                Map<String, Component> map = new HashMap<>();
                ConfigDataService service = Ari.instance.dataService;

                String PATTERN_DATETIME = "yyyy" + service.getValue("base.time-format.year") + "MM" + service.getValue("base.time-format.month") + "dd" + service.getValue("base.time-format.day") + "HH" + service.getValue("base.time-format.hour") +"mm" + service.getValue("base.time-format.minute") +"ss" + service.getValue("base.time-format.second");

                map.put(LangType.PLAYER_NAME.getType(), ComponentUtils.text(instance.getPlayerName()));
                map.put(LangType.FIRST_LOGIN_SERVER_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(instance.getFirstLoginTime(), PATTERN_DATETIME)));
                map.put(LangType.LAST_LOGIN_SERVER_TIME.getType(), ComponentUtils.text(TimeFormatUtils.format(instance.getLastLoginOffTime(), PATTERN_DATETIME)));
                map.put(LangType.TOTAL_ON_SERVER.getType(), ComponentUtils.text(TimeFormatUtils.format(instance.getTotalOnlineTime())));
                Player player = Bukkit.getPlayer(UUID.fromString(instance.getPlayerUUID()));
                map.put(LangType.PLAYER_WORLD.getType(), ComponentUtils.text(player == null ? Ari.instance.dataService.getValue("base.no-record"):player.getWorld().getName()));
                map.put(LangType.PLAYER_LOCATION.getType(), ComponentUtils.text(player == null ? Ari.instance.dataService.getValue("base.no-record"): FormatUtils.XYZText(player.getX(), player.getY(), player.getZ())));

                List<String> list = Ari.C_INSTANCE.getValue("server.player.info", FilePath.LANG, new TypeToken<List<String>>(){}.getType(), List.of());
                Component component = Component.join(
                        JoinConfiguration.separator(Component.newline()),
                        list.stream().map(s -> ComponentUtils.text(s, map)).toList()
                );

                sender.sendMessage(component);
            }).exceptionally(i -> {
                Log.error(i, "error");
                return null;
            });
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String permission() {
        return "ari.command.zako.info";
    }
}
