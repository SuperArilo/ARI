package com.tty.commands;

import com.tty.Ari;
import com.tty.entity.sql.ServerWarp;
import com.tty.enumType.FilePath;
import com.tty.function.WarpManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
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

public class setwarp extends BaseLiteralArgumentLiteralCommand {

    private final WarpManager warpManager = new WarpManager(true);

    public setwarp() {
        super(false, 2);
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
            player.sendMessage(ConfigUtils.t("function.warp.id-error"));
            return;
        }

        this.warpManager.getCountByPlayer(player.getUniqueId().toString())
                .thenCompose(serverWarps -> {
                    if (serverWarps.size() + 1 > PermissionUtils.getMaxCountInPermission(player, "warp")) {
                        player.sendMessage(ConfigUtils.t("function.warp.exceeds"));
                        return CompletableFuture.completedFuture(null);
                    }
                    return this.warpManager.getInstance(warpId);
                })
                .thenCompose(warp -> {
                    if (warp != null) {
                        player.sendMessage(ConfigUtils.t("function.warp.exist", player));
                        return CompletableFuture.completedFuture(null);
                    }
                    CompletableFuture<ServerWarp> futureWarp = new CompletableFuture<>();
                    Lib.Scheduler.runAtRegion(Ari.instance, player.getLocation(), task -> {
                        ServerWarp serverWarp = new ServerWarp();
                        serverWarp.setWarpId(warpId);
                        serverWarp.setWarpName(warpId);
                        serverWarp.setCreateBy(player.getUniqueId().toString());
                        serverWarp.setLocation(player.getLocation().toString());
                        serverWarp.setShowMaterial(PublicFunctionUtils.checkIsItem(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType()).name());
                        futureWarp.complete(serverWarp);
                    });
                    return futureWarp.thenCompose(warpManager::createInstance);
                })
                .thenAccept(status -> {
                    if(status == null) return;
                    if (status) {
                        player.sendMessage(ConfigUtils.t("function.warp.create-success"));
                    } else {
                        player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.save.on-error")));
                    }
                })
                .exceptionally(i -> {
                    Log.error(i, "create warp error");
                    player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-error")));
                    return null;
                });
    }

    @Override
    public String name() {
        return "setwarp";
    }

    @Override
    public String permission() {
        return "ari.command.setwarp";
    }
}
