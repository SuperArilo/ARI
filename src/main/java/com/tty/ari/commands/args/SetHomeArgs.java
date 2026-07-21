package com.tty.ari.commands.args;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.tty.ari.configuration.home.HomeConfig;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.entity.cache.PlayerHomeRepository;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name_id (string)", permission = "ari.command.sethome", tokenLength = 2)
@ArgumentCommand
public class SetHomeArgs extends RequiredArgumentCommand<String> {

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

        String homeId = args[1];
        Player player = (Player) sender;
        if (!this.checkEntityId(homeId)) {
            ConfigUtils.t("function.home.id-error", player).thenAccept(sender::sendMessage);
            return;
        }

        EntityRepository<ServerHome> repo = Ari.REPOSITORY_MANAGER.get(ServerHome.class);
        PlayerHomeRepository repository = (PlayerHomeRepository) repo;

        repository.queryCount(new LambdaQueryWrapper<>(ServerHome.class).eq(ServerHome::getPlayerUUID, player.getUniqueId().toString())).thenCompose(result -> {
            List<ServerHome> list = result.records();
            if (list.size() + 1 > Ari.PERMISSION_SERVICE.getMaxCountInPermission(player, "home")) {
                return ConfigUtils.t("function.home.exceeds", player).thenAccept(sender::sendMessage).thenApply(v -> null);
            }
            boolean exists = list.stream().anyMatch(i -> i.getHomeId().equals(homeId));
            if (exists) {
                return ConfigUtils.t("function.home.exist", player).thenAccept(sender::sendMessage).thenApply(v -> null);
            }

            CompletableFuture<ServerHome> buildHomeFuture = new CompletableFuture<>();

            Ari.instance.getScheduler().runAtRegion(player.getLocation(), task -> {
                ServerHome serverHome = new ServerHome();
                serverHome.setHomeId(homeId);
                serverHome.setHomeName(homeId);
                serverHome.setPlayerUUID(player.getUniqueId().toString());
                serverHome.setLocation(Ari.instance.getConfigurationManager().jsonToString(player.getLocation().serialize()));
                serverHome.setShowMaterial(PublicFunctionUtils.checkIsItem(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType()).name());
                buildHomeFuture.complete(serverHome);
            });

            return buildHomeFuture.thenCompose(i -> repository.create(i, PartitionKey.of(player.getUniqueId().toString())));
        }).thenCompose(status -> {
            if (status == null) {
                return CompletableFuture.completedFuture(null);
            }
            return ConfigUtils.t("function.home.create-success", player).thenAccept(sender::sendMessage);
        }).exceptionally(ex -> {
            Ari.instance.getLog().error(ex, "create home error");
            player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error"), player));
            return null;
        });

    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(HomeConfig.class).isEnable();
    }
}
