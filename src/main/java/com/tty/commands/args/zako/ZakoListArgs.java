package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.tty.commands.sub.zako.ZakoList.Build_Zako_List;

@CommandMeta(displayName = "page (int)", permission = "ari.command.zako.list", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoListArgs extends BaseRequiredArgumentLiteralCommand<Integer> {

    public ZakoListArgs() {
        super(IntegerArgumentType.integer(1));
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
        Build_Zako_List(sender, Integer.parseInt(args[2]));
    }
}
