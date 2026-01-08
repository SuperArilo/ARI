package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.function.BanPlayerManager;
import com.tty.function.PlayerManager;
import com.tty.function.WhitelistManager;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ZakoBaseArgs <T> extends BaseRequiredArgumentLiteralCommand<T> {

    protected static final PlayerManager PLAYER_MANAGER = new PlayerManager(true);
    protected static final WhitelistManager WHITELIST_MANAGER = new WhitelistManager(true);
    protected static final BanPlayerManager BAN_PLAYER_MANAGER = new BanPlayerManager(true);

    public ZakoBaseArgs(boolean allowConsole, Integer correctArgsLength, ArgumentType<T> type, boolean isSuggests) {
        super(allowConsole, correctArgsLength, type, isSuggests);
    }

    /**
     * 根据输入参数解析 UUID
     * @param value 玩家名字或 UUID
     * @return 玩家 UUID，如果不存在则返回 null
     */
    protected UUID parseUUID(String value) {
        AtomicReference<UUID> uuid = new AtomicReference<>(null);
        try {
            uuid.set(UUID.fromString(value));
        } catch (Exception ignored) {
        }
        if (uuid.get() == null) {
            try {
                uuid.set(Bukkit.getOfflinePlayer(value).getUniqueId());
            } catch (Exception e) {
                return null;
            }
        }
        return uuid.get();
    }

}
