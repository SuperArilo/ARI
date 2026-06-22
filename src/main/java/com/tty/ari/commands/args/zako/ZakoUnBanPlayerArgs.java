package com.tty.ari.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.ari.Ari;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.BanPlayer;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.tool.ConfigUtils;
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
    public int execute(CommandSender sender, String[] args) {
        UUID uuid = PublicFunctionUtils.parseUUID(args[2]);
        EntityRepository<BanPlayer> repository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        LambdaQueryWrapper<BanPlayer> wrapper = new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, uuid.toString());

        repository.get(wrapper, PartitionKey.global()).thenCompose(banPlayer -> {
            if (banPlayer == null) CompletableFuture.completedFuture(false);
            return repository.delete(wrapper, PartitionKey.global());
        }).thenAccept(status -> {
            if (status) {
                ConfigUtils.t("function.zako.ban-remove-success").thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t("function.zako.ban-remove-failure").thenAccept(sender::sendMessage);
            }
        }).exceptionally(e -> {
            Ari.instance.getLog().error("delete ban player uuid {} error.", uuid.toString(), e);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
