package com.tty.ari.commands.args;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.configuration.warp.WarpConfig;
import com.tty.ari.entity.ServerWarp;
import com.tty.ari.entity.cache.ServerWarpRepository;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name_id (string)", permission = "ari.command.setwarp", tokenLength = 2)
@ArgumentCommand
public class SetWarpArgs extends RequiredArgumentCommand<String> {

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
    public int execute(CommandSender sender, String[] args) {

        String warpId = args[1];
        Player player = (Player) sender;

        if(!this.isEntityIdValid(warpId)) {
            ConfigUtils.t("function.warp.id-error", player).thenAccept(player::sendMessage);
            return 0;
        }

        EntityRepository<ServerWarp> repo = Ari.REPOSITORY_MANAGER.get(ServerWarp.class);
        ServerWarpRepository repository = (ServerWarpRepository) repo;

        repository.queryCount(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getCreateBy, player.getUniqueId().toString()))
                .thenCompose(result -> {
                    int max = Ari.PERMISSION_SERVICE.getMaxCountInPermission(player, "warp");
                    if (result.total() + 1 > max) {
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

                    return repository.get(new LambdaQueryWrapper<>(ServerWarp.class).eq(ServerWarp::getWarpId, warpId), PartitionKey.global())
                            .thenCompose(existing -> {
                                if (existing != null) {
                                    return ConfigUtils.t("function.warp.exist", player)
                                            .thenAccept(player::sendMessage)
                                            .thenApply(v -> null);
                                }

                                CompletableFuture<ServerWarp> futureWarp = new CompletableFuture<>();

                                Ari.instance.getScheduler().runAtRegion(
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

                                return futureWarp.thenCompose(i -> repository.create(i, PartitionKey.global()));
                            });
                })
                .thenCompose(created -> {
                    if (created == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return ConfigUtils.t("function.warp.create-success", player).thenAccept(player::sendMessage);
                })
                .exceptionally(e -> {
                    Ari.instance.getLog().error(e);
                    ConfigUtils.t("base.on-error", player).thenAccept(player::sendMessage);
                    return null;
                });
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(WarpConfig.class).isEnable();
    }
}
