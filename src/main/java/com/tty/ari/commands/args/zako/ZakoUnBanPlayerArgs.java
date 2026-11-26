package com.tty.ari.commands.args.zako;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.persistence.CachedRepository;
import com.tty.api.persistence.query.Query;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.BanPlayer;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name | uuid (string)", permission = "ari.command.zako.unban", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoUnBanPlayerArgs extends RequiredArgumentCommand<String> {

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
        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);

        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return;
        }

        UUID uuid = offlinePlayer.getUniqueId();
        CachedRepository<BanPlayer, Long> repository = Ari.REPOSITORY_MANAGER.bans();
        Query<BanPlayer> query = Query.of(BanPlayer.class).eq(BanPlayer::getPlayerUUID, uuid.toString());

        repository.findOne(query).thenCompose(banPlayer -> {
            if (banPlayer.isEmpty()) return CompletableFuture.completedFuture(0);
            return repository.delete(query);
        }).thenAccept(count -> {
            if (count > 0) {
                ConfigUtils.t("function.zako.ban-remove-success").thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t("function.zako.ban-remove-failure").thenAccept(sender::sendMessage);
            }
        }).exceptionally(e -> {
            Ari.instance.getLog().error("delete ban player uuid {} error.", uuid.toString(), e);
            return null;
        });

    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
