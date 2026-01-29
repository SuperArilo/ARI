package com.tty.commands.args;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.ServerHome;
import com.tty.entity.cache.PlayerHomeRepository;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name (string)", permission = "ari.command.sethome", tokenLength = 2)
@ArgumentCommand
public class SetHomeArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public SetHomeArgs() {
        super( StringArgumentType.string());
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
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.HOME_CONFIG.name()))) return;

        String homeId = args[1];
        Player player = (Player) sender;
        if (!FormatUtils.checkIdName(homeId)) {
            ConfigUtils.t("function.home.id-error", player).thenAccept(sender::sendMessage);
            return;
        }

        EntityRepository<ServerHome> repo = Ari.REPOSITORY_MANAGER.get(ServerHome.class);
        PlayerHomeRepository repository = (PlayerHomeRepository) repo;

        repository.queryCount(new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getPlayerUUID, player.getUniqueId().toString()))
                .thenCompose(result -> {
                    List<ServerHome> list = result.getRecords();

                    if (list.size() + 1 > PermissionUtils.getMaxCountInPermission(player, "home")) {
                        return ConfigUtils.t("function.home.exceeds", player)
                                .thenAccept(sender::sendMessage)
                                .thenApply(v -> null);
                    }

                    boolean exists = list.stream().anyMatch(i -> i.getHomeId().equals(homeId));

                    if (exists) {
                        return ConfigUtils.t("function.home.exist", player)
                                .thenAccept(sender::sendMessage)
                                .thenApply(v -> null);
                    }

                    CompletableFuture<ServerHome> buildHomeFuture = new CompletableFuture<>();

                    Lib.Scheduler.runAtRegion(Ari.instance, player.getLocation(), task -> {
                        ServerHome serverHome = new ServerHome();
                        serverHome.setHomeId(homeId);
                        serverHome.setHomeName(homeId);
                        serverHome.setPlayerUUID(player.getUniqueId().toString());
                        serverHome.setLocation(player.getLocation().toString());
                        serverHome.setShowMaterial(
                                PublicFunctionUtils
                                        .checkIsItem(player.getLocation()
                                                .getBlock()
                                                .getRelative(BlockFace.DOWN)
                                                .getType())
                                        .name()
                        );
                        buildHomeFuture.complete(serverHome);
                    });

                    return buildHomeFuture.thenCompose(repository::create);
                })
                .thenCompose(status -> {
                    if (status == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return ConfigUtils.t("function.home.create-success", player).thenAccept(sender::sendMessage);
                })
                .exceptionally(ex -> {
                    Log.error(ex, "create home error");
                    ConfigUtils.t("base.on-error", player).thenAccept(player::sendMessage);
                    return null;
                });
    }

}
