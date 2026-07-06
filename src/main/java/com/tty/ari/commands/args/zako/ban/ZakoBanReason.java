package com.tty.ari.commands.args.zako.ban;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.BanPlayer;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@CommandMeta(displayName = "reason (string)", permission = "ari.command.zako.ban",  tokenLength = 8, allowConsole = true)
@ArgumentCommand
public class ZakoBanReason extends RequiredArgumentCommand<String> {

    private static final TimeUnit[] TIME_UNITS = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.greedyString();
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
    protected boolean isEnableInGame() {
        return true;
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (args.length != 8) return 0;

        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);

        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return 0;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        EntityRepository<BanPlayer> banRepository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        EntityRepository<WhitelistInstance> whitelistRepository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);

        banRepository.get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, uuid.toString()), PartitionKey.global()).thenCompose(banPlayer -> {
                boolean b = banPlayer == null;
                return ConfigUtils.t("function.zako.had-banned").thenAccept(m -> {
                    if (!b) {
                        sender.sendMessage(m);
                    }
                }).thenApply(i -> b);
            }).thenCompose(status -> {

                if (!status) return CompletableFuture.completedFuture(false);
                long now = System.currentTimeMillis();

                long total = 0;
                for (int i = 0; i < TIME_UNITS.length; i++) {
                    int index = 3 + i;
                    try {
                        int value = Integer.parseInt(args[index]);
                        if (value < 0) return CompletableFuture.completedFuture(false);
                        total = Math.addExact(total, TIME_UNITS[i].toMillis(value));
                    } catch (Exception e) {
                        Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.number.format-error"));
                        return CompletableFuture.completedFuture(false);
                    }
                }

                BanPlayer banPlayer = new BanPlayer();
                banPlayer.setPlayerUUID(uuid.toString());
                banPlayer.setOperator(Operator.getOperator(sender).toString());
                banPlayer.setReason(args[7]);
                banPlayer.setStartTime(now);
                banPlayer.setEndTime(now + total);

                LambdaQueryWrapper<WhitelistInstance> wrapper = new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString());


                return banRepository.create(banPlayer, PartitionKey.global()).thenApply(i -> {
                   if(i == null) {
                       return false;
                   }
                    whitelistRepository.delete(wrapper, PartitionKey.global());
                    return true;
                });
            }).thenAccept(status -> {
                if (status) {
                    OfflinePlayer kickPlayer = PlayerCache.getPlayer(uuid);
                    if (kickPlayer instanceof Player player) {
                        Ari.instance.getScheduler().runAtEntity(player, i -> player.kick(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed"), player)),  null);
                    }
                    ConfigUtils.t("function.zako.banned", kickPlayer).thenAccept(m -> Bukkit.getServer().getOnlinePlayers().stream().filter(i -> !i.equals(kickPlayer)).forEach(i -> i.sendMessage(m)));
                }
            }).exceptionally(e -> {
                ConfigUtils.t("function.zako.add-failure").thenAccept(sender::sendMessage);
                Ari.instance.getLog().error(e);
                return null;
            });
        return Command.SINGLE_SUCCESS;
    }
}
