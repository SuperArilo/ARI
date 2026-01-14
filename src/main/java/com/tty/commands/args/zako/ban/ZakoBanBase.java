package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.Ari;
import com.tty.entity.BanPlayer;
import com.tty.entity.WhitelistInstance;
import com.tty.function.BanPlayerManager;
import com.tty.function.WhitelistManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.enum_type.FilePath;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.lib.tool.TimeFormatUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class ZakoBanBase <T> extends BaseRequiredArgumentLiteralCommand<T> {

    public ZakoBanBase(boolean allowConsole, Integer correctArgsLength, ArgumentType<T> type, boolean isSuggests) {
        super(allowConsole, correctArgsLength, type, isSuggests);
    }

    public void ban(CommandSender sender, String[] args) {
        UUID uuid = PublicFunctionUtils.parseUUID(args[2]);

        if (uuid == null) {
            sender.sendMessage(ConfigUtils.t("function.zako.zako-not-exist"));
            return;
        }
        EntityRepository<Object, BanPlayer> banPlayerRepository = Ari.REPOSITORY_MANAGER.get(BanPlayer.class);
        EntityRepository<Object, WhitelistInstance> whitelistInstanceRepository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);

        banPlayerRepository.get(new BanPlayerManager.QueryKey(uuid.toString()))
            .thenCompose(banPlayer -> {
                if (banPlayer != null) {
                    sender.sendMessage(ConfigUtils.t("function.zako.had_baned"));
                    return CompletableFuture.completedFuture(false);
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
                whitelistInstanceRepository.get(new WhitelistManager.QueryKey(uuid.toString())).thenCompose(whitelistInstanceRepository::delete);

                String string = TimeFormatUtils.format(total);
                Lib.Scheduler.run(Ari.instance, i -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.kick(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-player.data-changed")));

                        Bukkit.getServer().broadcast(ConfigUtils.t("function.zako.baned", Map.of(LangType.BAN_T0TAL_TIME.getType(), Component.text(string), LangType.BAN_REASON.getType(), ComponentUtils.text(args[3]))));
                        Log.debug("baned player uuid %s. total %s", uuid.toString(), string);
                    }

                });

            }).exceptionally(e -> {
                Log.error(e, Ari.C_INSTANCE.getValue("function.zako.add-failure", FilePath.Lang));
                return null;
            });
    }

}
