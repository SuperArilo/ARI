package com.tty.commands.args.zako.ban;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.command.RequiredArgumentCommand;
import com.tty.entity.BanPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.api.utils.TimeFormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ZakoBanBase<T> extends RequiredArgumentCommand<T> {

    private static final TimeUnit[] TIME_UNITS = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};

    public void ban(CommandSender sender, String[] args) {
        UUID uuid = PublicFunctionUtils.parseUUID(args[2]);

        if (uuid == null) {
            if (sender instanceof Player player) {
                ConfigUtils.t("function.zako.zako-not-exist", player).thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t("function.zako.zako-not-exist").thenAccept(sender::sendMessage);
            }
            return;
        }
        EntityRepository<BanPlayer> banPlayerRepository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        EntityRepository<WhitelistInstance> whitelistInstanceRepository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);

        banPlayerRepository.get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, uuid.toString()), PartitionKey.global())
            .thenCompose(banPlayer -> {
                if (banPlayer != null) {
                    CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.had_baned", player):ConfigUtils.t("function.zako.had_baned");
                    return future.thenAccept(sender::sendMessage).thenApply(i -> banPlayer);
                }
                return CompletableFuture.completedFuture(null);
            })
            .thenAccept(s -> {
                if (s != null) return;
                long now = System.currentTimeMillis();

                AtomicLong total = new AtomicLong(0);
                for (int i = 0; i < TIME_UNITS.length; i++) {
                    int index = 4 + i;
                    if (args.length > index) {
                        int value = Integer.parseInt(args[index]);
                        if (value < 0) {
                            return;
                        }
                        total.addAndGet(TimeFormatUtils.toTimestamp(value, TIME_UNITS[i]));
                    }
                }

                BanPlayer banPlayer = new BanPlayer();
                banPlayer.setPlayerUUID(uuid.toString());
                banPlayer.setOperator(Operator.getOperator(sender).toString());
                banPlayer.setReason(args[3]);
                banPlayer.setStartTime(now);
                banPlayer.setEndTime(now + total.get());

                banPlayerRepository.create(banPlayer, PartitionKey.global());
                LambdaQueryWrapper<WhitelistInstance> wrapper = new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString());
                //同时移除白名单
                whitelistInstanceRepository.delete(wrapper, PartitionKey.global());

                Ari.SCHEDULER.run(Ari.instance, i -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    if (offlinePlayer instanceof Player player) {
                        player.kick(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
                        Ari.LOG.debug("baned player uuid {}. total {}", uuid.toString(), TimeFormatUtils.format(total.get()));
                    }
                    ConfigUtils.t("function.zako.baned", offlinePlayer).thenAccept(Bukkit::broadcast);
                });

            }).exceptionally(e -> {
                ConfigUtils.t("function.zako.add-failure").thenAccept(sender::sendMessage);
                Ari.LOG.error(e);
                return null;
            });
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
