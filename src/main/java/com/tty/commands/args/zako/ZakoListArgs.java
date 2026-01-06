package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.tty.commands.sub.zako.ZakoList.Build_Zako_List;

public class ZakoListArgs extends ZakoBaseArgs<Integer> {

    public ZakoListArgs() {
        super(true, 3, IntegerArgumentType.integer(1), true);
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
    public String name() {
        return "list";
    }

    @Override
    public String permission() {
        return "ari.command.zako.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Build_Zako_List(this.whitelistManager, sender, Integer.parseInt(args[2]));
    }
}
