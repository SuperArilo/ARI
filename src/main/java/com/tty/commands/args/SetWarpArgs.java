package com.tty.commands.args;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.sql.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.function.WarpManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.lib.tool.PermissionUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SetWarpArgs extends BaseRequiredArgumentLiteralCommand<String> {

    private final WarpManager warpManager = new WarpManager(true);

    public SetWarpArgs() {
        super(2, StringArgumentType.string());
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
        return "name (string)";
    }

    @Override
    public String permission() {
        return "ari.command.setwarp";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.WARP_CONFIG.name()))) return;

        String warpId = args[1];
        Player player = (Player) sender;

        if(!FormatUtils.checkIdName(warpId)) {
            player.sendMessage(ConfigUtils.t("function.warp.id-error"));
            return;
        }

        this.warpManager.getCountByPlayer(player.getUniqueId().toString())
            .thenCompose(list -> {
                int max = PermissionUtils.getMaxCountInPermission(player, "warp");
                if (list.size() + 1 > max) {
                    player.sendMessage(ConfigUtils.t("function.warp.exceeds"));
                    return CompletableFuture.completedFuture(false);
                }
                return CompletableFuture.completedFuture(true);
            })
            .thenCompose(shouldProceed -> {
                if (!shouldProceed) {
                    return CompletableFuture.completedFuture(false);
                }
                return this.warpManager.getInstance(warpId)
                    .thenCompose(existing -> {
                        if (existing != null) {
                            player.sendMessage(ConfigUtils.t("function.warp.exist", player));
                            return CompletableFuture.completedFuture(false);
                        }
                        CompletableFuture<ServerWarp> futureWarp = new CompletableFuture<>();
                        Lib.Scheduler.runAtRegion(Ari.instance, player.getLocation(), task -> {
                            ServerWarp serverWarp = new ServerWarp();
                            serverWarp.setWarpId(warpId);
                            serverWarp.setWarpName(warpId);
                            serverWarp.setCreateBy(player.getUniqueId().toString());
                            serverWarp.setLocation(player.getLocation().toString());
                            serverWarp.setShowMaterial(PublicFunctionUtils.checkIsItem( player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType()).name());
                            futureWarp.complete(serverWarp);
                        });
                        return futureWarp.thenCompose(warpManager::createInstance);
                    });
            })
            .thenAccept(created -> {
                if (Boolean.TRUE.equals(created)) {
                    player.sendMessage(ConfigUtils.t("function.warp.create-success"));
                }
            })
            .exceptionally(e -> {
                Log.error(e, "create warp error");
                player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-error")));
                return null;
            });

    }
}
