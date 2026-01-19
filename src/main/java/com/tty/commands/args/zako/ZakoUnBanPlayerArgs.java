package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.BanPlayer;
import com.tty.function.BanPlayerManager;
import com.tty.lib.Log;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ZakoUnBanPlayerArgs extends BaseRequiredArgumentLiteralCommand<String> {

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
            if (sender instanceof Player player) {
                ConfigUtils.t("function.zako.zako-not-exist", player).thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t("function.zako.zako-not-exist").thenAccept(sender::sendMessage);
            }
            return;
        }
        EntityRepository<Object, BanPlayer> repository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        repository.get(new BanPlayerManager.QueryKey(uuid.toString()))
            .thenCompose(banPlayer -> {
                if (banPlayer == null) {
                    CompletableFuture<Component> future = (sender instanceof Player player) ?ConfigUtils.t("function.zako.ban-remove-failure", player):ConfigUtils.t("function.zako.ban-remove-failure");
                    return future.thenAccept(sender::sendMessage).thenApply(v -> false);
                }

                return repository.delete(banPlayer)
                    .thenCompose(deleted -> {
                        if (!deleted) {
                            CompletableFuture<Component> msgFuture =
                                    (sender instanceof Player player)
                                            ? ConfigUtils.t("function.zako.ban-remove-failure", player)
                                            : ConfigUtils.t("function.zako.ban-remove-failure");

                            return msgFuture
                                    .thenAccept(sender::sendMessage)
                                    .thenApply(v -> false);
                        }

                        CompletableFuture<Component> successFuture =
                                (sender instanceof Player player)
                                        ? ConfigUtils.t("function.zako.ban-remove-success", player)
                                        : ConfigUtils.t("function.zako.ban-remove-success");

                        return successFuture
                                .thenAccept(sender::sendMessage)
                                .thenApply(v -> true);
                    });
            })
            .exceptionally(e -> {
                Log.error("delete ban player uuid {} error.", uuid.toString(), e);
                return null;
            });
    }
}
