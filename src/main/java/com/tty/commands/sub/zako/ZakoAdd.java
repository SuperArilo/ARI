package com.tty.commands.sub.zako;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.Ari;
import com.tty.entity.sql.WhitelistInstance;
import com.tty.function.WhitelistManager;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class ZakoAdd extends ZakoBase<String> {

    public ZakoAdd(boolean allowConsole, ArgumentType<String> type) {
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

        WhitelistInstance instance = new WhitelistInstance();
        instance.setPlayerUUID(uuid.toString());
        instance.setAddTime(System.currentTimeMillis());

        WhitelistManager manager = new WhitelistManager(true);
        manager.getInstance(uuid.toString()).thenAccept(i -> {
            if (i != null) {
                sender.sendMessage(ConfigUtils.t("function.zako.player-exist"));
                return;
            }
            manager.createInstance(instance).thenAccept(status ->
                            sender.sendMessage(ConfigUtils.t("function.zako.add-" + (status ? "success":"failure"))))
                    .exceptionally(n -> {
                        Log.error(n, "add zako error");
                        sender.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
                        return null;
                    });
        }).exceptionally(i -> {
            Log.error(i, "query zako error");
            sender.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
            return null;
        });
    }

    @Override
    public String name() {
        return "add";
    }

    @Override
    public String permission() {
        return "ari.command.zako.add";
    }
}
