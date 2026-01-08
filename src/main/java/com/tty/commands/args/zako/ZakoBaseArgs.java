package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.function.BanPlayerManager;
import com.tty.function.PlayerManager;
import com.tty.function.WhitelistManager;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;

public abstract class ZakoBaseArgs <T> extends BaseRequiredArgumentLiteralCommand<T> {

    protected static final PlayerManager PLAYER_MANAGER = new PlayerManager(true);
    protected static final WhitelistManager WHITELIST_MANAGER = new WhitelistManager(true);
    protected static final BanPlayerManager BAN_PLAYER_MANAGER = new BanPlayerManager(true);

    public ZakoBaseArgs(boolean allowConsole, Integer correctArgsLength, ArgumentType<T> type, boolean isSuggests) {
        super(allowConsole, correctArgsLength, type, isSuggests);
    }

}
