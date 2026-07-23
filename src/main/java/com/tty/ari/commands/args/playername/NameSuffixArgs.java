package com.tty.ari.commands.args.playername;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.ComponentTool;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.tool.PlayerCache;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "content (string)", permission = "ari.command.playername", tokenLength = 4, allowConsole = true)
@LiteralCommand
public class NameSuffixArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);
        if (offlinePlayer == null) {
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return;
        }
        String uuid = offlinePlayer.getUniqueId().toString();
        String value = args[3];
        EntityRepository<ServerPlayer> repository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);
        LambdaQueryWrapper<ServerPlayer> wrapper = new LambdaQueryWrapper<ServerPlayer>().eq(ServerPlayer::getPlayerUUID, uuid);
        PartitionKey key = PartitionKey.global();
        repository.get(wrapper, key).thenCompose(serverPlayer -> {
            if (serverPlayer == null) {
                return CompletableFuture.completedFuture(false);
            }
            serverPlayer.setNameSuffix(value);
            return repository.update(serverPlayer, wrapper, key);
        }).thenAccept(status -> {
            if (status) {
                sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.command.execute-success")));
            } else {
                TextComponent append = ComponentTool.text(Ari.DATA_SERVICE.getValue("base.command.execute-success")).append(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                sender.sendMessage(append);
            }
        }).exceptionally(e -> {
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-error")));
            Ari.instance.getLog().error(e);
            return null;
        });
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
