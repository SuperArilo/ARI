package com.tty.commands.args;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.ServerHome;
import com.tty.enumType.FilePath;
import com.tty.function.HomeManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
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

public class SetHomeArgs extends BaseRequiredArgumentLiteralCommand<String> {

    public SetHomeArgs() {
        super(2, StringArgumentType.string());
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
        return "name (string)";
    }

    @Override
    public String permission() {
        return "ari.command.sethome";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.HOME_CONFIG.name()))) return;

        String homeId = args[1];
        if(FormatUtils.checkIdName(homeId)) {
            Player player = (Player) sender;
            EntityRepository<Object, ServerHome> repository = Ari.REPOSITORY_MANAGER.get(ServerHome.class);
            repository.getAllForCheck(new HomeManager.QueryKey(player.getUniqueId().toString(), null))
                .thenCompose(result -> {
                    List<ServerHome> list = result.getRecords();
                    if (list.size() + 1 > PermissionUtils.getMaxCountInPermission(player, "home")) {
                        sender.sendMessage(ConfigUtils.t("function.home.exceeds"));
                        return CompletableFuture.completedFuture(null);
                    }
                    long sameHome = list.stream().filter(i -> i.getHomeId().equals(homeId)).count();
                    if (sameHome == 1) {
                        sender.sendMessage(ConfigUtils.t("function.home.exist", player));
                        return CompletableFuture.completedFuture(null);
                    }

                    CompletableFuture<ServerHome> future = new CompletableFuture<>();
                    Lib.Scheduler.runAtRegion(Ari.instance, player.getLocation(), task -> {
                        ServerHome serverHome = new ServerHome();
                        serverHome.setHomeId(homeId);
                        serverHome.setHomeName(homeId);
                        serverHome.setPlayerUUID(player.getUniqueId().toString());
                        serverHome.setLocation(player.getLocation().toString());
                        serverHome.setShowMaterial(PublicFunctionUtils.checkIsItem(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType()).name());
                        future.complete(serverHome);
                    });
                    return future.thenCompose(repository::create);
                })
                .thenAccept(status -> {
                    if (status == null) return;
                    sender.sendMessage(ConfigUtils.t("function.home.create-success", player));
                }).exceptionally(i -> {
                    Log.error(i, "create home error");
                    player.sendMessage(Ari.instance.dataService.getValue("base.on-error"));
                    return null;
                });
        } else {
            sender.sendMessage(ConfigUtils.t("function.home.id-error"));
        }
    }
}
