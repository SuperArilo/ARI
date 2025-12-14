package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.sql.WhitelistInstance;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class ZakoAddArgs extends ZakoBaseArgs<String> {

    public ZakoAddArgs() {
        super(true, 3, StringArgumentType.string(), false);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "name | uuid (string)";
    }

    @Override
    public String permission() {
        return "ari.command.zako.add";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String value = args[2];
        UUID uuid = this.parseUUID(value);
        if (uuid == null) return;

        WhitelistInstance instance = new WhitelistInstance();
        instance.setPlayerUUID(uuid.toString());
        instance.setAddTime(System.currentTimeMillis());

        this.manager.getInstance(uuid.toString()).thenAccept(i -> {
            if (i != null) {
                sender.sendMessage(ConfigUtils.t("function.zako.player-exist"));
                return;
            }
            this.manager.createInstance(instance).thenAccept(status ->
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
}
