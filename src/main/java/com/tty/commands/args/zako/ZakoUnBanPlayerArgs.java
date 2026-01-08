package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ZakoUnBanPlayerArgs extends ZakoBaseArgs<String> {

    public ZakoUnBanPlayerArgs() {
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
        return "ari.command.zako.unban";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        UUID uuid = PublicFunctionUtils.parseUUID(args[2]);
        if (uuid == null) {
            sender.sendMessage(ConfigUtils.t("function.zako.zako-not-exist"));
            return;
        }
        BAN_PLAYER_MANAGER.getInstance(uuid.toString())
            .thenCompose(CompletableFuture::completedFuture)
            .thenAccept(banPlayer -> {
                if (banPlayer == null) {
                    sender.sendMessage(ConfigUtils.t("function.zako.ban-remove-failure"));
                    return;
                }
                BAN_PLAYER_MANAGER.deleteInstance(banPlayer);
                sender.sendMessage(ConfigUtils.t("function.zako.ban-remove-success"));
            })
            .whenComplete((i, e) -> {
                if (e == null) return;
                Log.error("delete ban player uuid %s error.", uuid.toString());
            });
    }
}
