package com.tty.ari.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.ServerHome;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name | uuid (string)", permission = "ari.command.zako.removeprofile", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoRemoveProfileArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);

        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return 0;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        if (offlinePlayer instanceof Player player && player.isOnline()) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.fail-by-player-online")));
            return 0;
        }

        PartitionKey partitionKey = PartitionKey.of(uuid);

        CompletableFuture<Integer> f1 = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class).delete(new LambdaQueryWrapper<ServerPlayer>().eq(ServerPlayer::getPlayerUUID, uuid), partitionKey);
        CompletableFuture<Integer> f2 = Ari.REPOSITORY_MANAGER.get(ServerHome.class).delete(new LambdaQueryWrapper<ServerHome>().eq(ServerHome::getPlayerUUID, uuid), partitionKey);
        CompletableFuture<Integer> f3 = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class).delete(new LambdaQueryWrapper<WhitelistInstance>().eq(WhitelistInstance::getPlayerUUID, uuid), partitionKey);

        try {
            CompletableFuture.allOf(f1, f2, f3).join();
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.command.execute-success")));
        } catch (Exception e) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error")));
            Ari.instance.getLog().error(e);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
