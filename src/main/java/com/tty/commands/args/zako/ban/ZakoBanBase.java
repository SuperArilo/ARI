package com.tty.commands.args.zako.ban;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.entity.BanPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.api.Log;
import com.tty.api.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.enum_type.FilePath;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.PublicFunctionUtils;
import com.tty.api.TimeFormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class ZakoBanBase<T> extends BaseRequiredArgumentLiteralCommand<T> {

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

        banPlayerRepository.get(new LambdaQueryWrapper<>(BanPlayer.class).eq(BanPlayer::getPlayerUUID, uuid.toString()))
            .thenCompose(banPlayer -> {
                if (banPlayer != null) {
                    CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.had_baned", player):ConfigUtils.t("function.zako.had_baned");
                    return future.thenAccept(sender::sendMessage).thenApply(i -> false);
                }
                return CompletableFuture.completedFuture(true);
            })
            .thenAccept(s -> {
                if (!s) return;
                long now = System.currentTimeMillis();

                TimeUnit[] units = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};

                long total = 0;
                for (int i = 0; i < units.length; i++) {
                    int index = 4 + i;
                    if (args.length > index) {
                        int value = Integer.parseInt(args[index]);
                        if (value < 0) {
                            return;
                        }
                        total += TimeFormatUtils.toTimestamp(value, units[i]);
                    }
                }

                BanPlayer banPlayer = new BanPlayer();
                banPlayer.setPlayerUUID(uuid.toString());
                banPlayer.setOperator(Operator.getOperator(sender).toString());
                banPlayer.setReason(args[3]);
                banPlayer.setStartTime(now);
                banPlayer.setEndTime(now + total);

                banPlayerRepository.create(banPlayer);
                //同时移除白名单
                whitelistInstanceRepository.get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString())).thenCompose(whitelistInstanceRepository::delete);

                String string = TimeFormatUtils.format(total);
                Ari.SCHEDULER.run(Ari.instance, i -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.kick(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed")));
                        ConfigUtils.t("function.zako.baned", player).thenAccept(Bukkit::broadcast);
                        Log.debug("baned player uuid {}. total {}", uuid.toString(), string);
                    }
                });

            }).exceptionally(e -> {
                Log.error(e, Ari.C_INSTANCE.getValue("function.zako.add-failure", FilePath.Lang));
                return null;
            });
    }

}
