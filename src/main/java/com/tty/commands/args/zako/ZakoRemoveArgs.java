package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ZakoRemoveArgs extends ZakoBaseArgs<String> {

    public ZakoRemoveArgs() {
        super(true, 3, StringArgumentType.string(), false);
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(Set.of());
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
        return "ari.command.zako.remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String value = args[2];
        UUID uuid = PublicFunctionUtils.parseUUID(value);
        if (uuid == null) return;

        WHITELIST_MANAGER.getInstance(uuid.toString()).thenCompose(instance -> {
            if (instance == null) {
                return CompletableFuture.completedFuture(false);
            }
            return WHITELIST_MANAGER.deleteInstance(instance);
        }).thenAccept(status -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                Lib.Scheduler.runAtEntity(Ari.instance, player, (i)->player.kick(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-player.data-changed"))), () -> player.sendMessage(ConfigUtils.t("on-error")));
            }
            sender.sendMessage(ConfigUtils.t("function.zako.whitelist-remove-" + (status ? "success":"failure")));
        }).exceptionally(i -> {
            Log.error(i, "remove zako error");
            sender.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
            return null;
        });
    }
}
