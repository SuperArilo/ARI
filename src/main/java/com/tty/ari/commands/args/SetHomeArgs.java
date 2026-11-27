package com.tty.ari.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.persistence.OwnedRepository;
import com.tty.api.persistence.query.Query;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.configuration.home.HomeConfig;
import com.tty.ari.entity.ServerHome;
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

        OwnedRepository<ServerHome, Long> homes = Ari.REPOSITORY_MANAGER.homes().forOwner(player.getUniqueId());

        homes.count(Query.of(ServerHome.class)).thenCompose(count -> {
            if (count + 1 > Ari.PERMISSION_SERVICE.getMaxCountInPermission(player, "home")) {
                return ConfigUtils.t("function.home.exceeds", player).thenAccept(sender::sendMessage).thenApply(v -> null);
            }
            return homes.findOne(Query.of(ServerHome.class).eq(ServerHome::getHomeId, homeId)).thenCompose(existing -> {
                if (existing.isPresent()) {
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

                return buildHomeFuture.thenCompose(homes::insert);
            });
        }).thenAccept(created -> {
            if (created == null) {
                return;
            }
            ConfigUtils.t("function.home.create-success", player).thenAccept(sender::sendMessage);
        }).exceptionally(ex -> {
            Ari.instance.getLog().error(ex, "create home error");
            player.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-error"), player));
            return null;
        });

    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(HomeConfig.class).isEnable();
    }
}
