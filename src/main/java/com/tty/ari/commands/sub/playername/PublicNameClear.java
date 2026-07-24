package com.tty.ari.commands.sub.playername;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.ComponentTool;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import com.tty.ari.tool.PlayerCache;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class PublicNameClear extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

    public void setValue(CommandSender sender, String[] args, PlaceholderPlayer placeholder) {
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);
        if (offlinePlayer == null) {
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return;
        }
        String uuid = offlinePlayer.getUniqueId().toString();
        EntityRepository<ServerPlayer> repository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);
        LambdaQueryWrapper<ServerPlayer> wrapper = new LambdaQueryWrapper<ServerPlayer>().eq(ServerPlayer::getPlayerUUID, uuid);
        PartitionKey key = PartitionKey.global();
        repository.get(wrapper, key).thenCompose(serverPlayer -> {
            if (serverPlayer == null) {
                return CompletableFuture.completedFuture(false);
            }
            return switch (placeholder) {
                case PLAYER_NAME_PREFIX -> {
                    serverPlayer.setNamePrefix(null);
                    yield repository.update(serverPlayer, wrapper, key);
                }
                case PLAYER_NAME_SUFFIX -> {
                    serverPlayer.setNameSuffix(null);
                    yield repository.update(serverPlayer, wrapper, key);
                }
                default -> CompletableFuture.completedFuture(false);
            };
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

}
