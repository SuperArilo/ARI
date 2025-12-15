package com.tty.commands.args.zako.ban;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.Ari;
import com.tty.commands.args.zako.ZakoBaseArgs;
import com.tty.entity.sql.BanPlayer;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FilePath;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.tool.ComponentUtils;
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

public abstract class ZakoBanBase <T> extends ZakoBaseArgs<T> {

    public ZakoBanBase(boolean allowConsole, Integer correctArgsLength, ArgumentType<T> type, boolean isSuggests) {
        super(allowConsole, correctArgsLength, type, isSuggests);
    }

    public void ban(CommandSender sender, String[] args) {
        UUID uuid = this.parseUUID(args[2]);

        if (uuid == null) {
            sender.sendMessage(ConfigUtils.t("function.zako.player-not-exist"));
            return;
        }

        this.banPlayerManager.getInstance(uuid.toString())
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

                TimeUnit[] units = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES};

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

                this.banPlayerManager.createInstance(banPlayer);
                //同时移除白名单
                this.whitelistManager.getInstance(uuid.toString()).thenCompose(this.whitelistManager::deleteInstance);

                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Lib.Scheduler.runAtEntity(Ari.instance, player, i -> player.kick(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-player.data-changed"))), null);
                }

                String string = TimeFormatUtils.format(total);
                sender.sendMessage(ConfigUtils.t("function.zako.baned", Map.of(LangType.BAN_T0TAL_TIME.getType(), Component.text(string))));
                Log.debug("baned player uuid %s. total %s", uuid.toString(), string);
            }).exceptionally(e -> {
                Log.error(e, Ari.C_INSTANCE.getValue("function.zako.add-failure", FilePath.Lang));
                return null;
            });
    }

}
