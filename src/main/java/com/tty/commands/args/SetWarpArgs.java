package com.tty.commands.args;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.ServerWarp;
import com.tty.entity.cache.ServerWarpRepository;
import com.tty.enumType.FilePath;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PermissionUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name (string)", permission = "ari.command.setwarp", tokenLength = 2)
@ArgumentCommand
public class SetWarpArgs extends BaseRequiredArgumentLiteralCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.WARP_CONFIG.name()))) return;

        String warpId = args[1];
        Player player = (Player) sender;

        if(!FormatUtils.checkIdName(warpId)) {
            ConfigUtils.t("function.warp.id-error", player).thenAccept(player::sendMessage);
            return;
        }

        EntityRepository<ServerWarp> repo = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);
        ServerWarpRepository repository = (ServerWarpRepository) repo;

        repository.queryCount(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getCreateBy, player.getUniqueId().toString()))
                .thenCompose(result -> {
                    int max = PermissionUtils.getMaxCountInPermission(player, "warp");
                    if (result.getTotal() + 1 > max) {
                        return ConfigUtils.t("function.warp.exceeds", player)
                                .thenAccept(player::sendMessage)
                                .thenApply(v -> false);
                    }
                    return CompletableFuture.completedFuture(true);
                })
                .thenCompose(shouldProceed -> {
                    if (!shouldProceed) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return repository.get(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpId))
                            .thenCompose(existing -> {
                                if (existing != null) {
                                    return ConfigUtils.t("function.warp.exist", player)
                                            .thenAccept(player::sendMessage)
                                            .thenApply(v -> null);
                                }

                                CompletableFuture<ServerWarp> futureWarp = new CompletableFuture<>();

                                Lib.Scheduler.runAtRegion(
                                        Ari.instance,
                                        player.getLocation(),
                                        task -> {
                                            ServerWarp serverWarp = new ServerWarp();
                                            serverWarp.setWarpId(warpId);
                                            serverWarp.setWarpName(warpId);
                                            serverWarp.setCreateBy(player.getUniqueId().toString());
                                            serverWarp.setLocation(player.getLocation().toString());
                                            serverWarp.setShowMaterial(
                                                    PublicFunctionUtils
                                                            .checkIsItem(
                                                                    player.getLocation()
                                                                            .getBlock()
                                                                            .getRelative(BlockFace.DOWN)
                                                                            .getType()
                                                            ).name()
                                            );
                                            futureWarp.complete(serverWarp);
                                        }
                                );

                                return futureWarp.thenCompose(repository::create);
                            });
                })
                .thenCompose(created -> {
                    if (created == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return ConfigUtils.t("function.warp.create-success", player).thenAccept(player::sendMessage);
                })
                .exceptionally(e -> {
                    Log.error(e);
                    ConfigUtils.t("base.on-error", player).thenAccept(player::sendMessage);
                    return null;
                });
    }
}
