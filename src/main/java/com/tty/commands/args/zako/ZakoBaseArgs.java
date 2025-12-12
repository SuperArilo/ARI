package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.ArgumentType;
import com.tty.function.WhitelistManager;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;

public abstract class ZakoBaseArgs <T> extends BaseRequiredArgumentLiteralCommand<T> {

    protected final WhitelistManager manager = new WhitelistManager(true);

    public ZakoBaseArgs(boolean allowConsole, Integer correctArgsLength, ArgumentType<T> type, boolean isSuggests) {
        super(allowConsole, correctArgsLength, type, isSuggests);
    }

}
