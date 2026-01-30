package com.tty.command;


import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.tty.Ari;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.command.ArgumentCommand;
import com.tty.api.command.SuperHandsomeCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;


@SuppressWarnings("UnstableApiUsage")
public abstract class RequiredArgumentCommand<T> extends PreCommand implements ArgumentCommand {

    @Override
    public CommandNode<CommandSourceStack> toBrigadier() {
        CommandMeta meta = this.getClass().getAnnotation(CommandMeta.class);
        if (meta == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " lost @CommandMeta");
        }
        RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(meta.displayName(), this.argumentType());
        builder.requires(ctx -> Ari.PERMISSION_SERVICE.hasPermission(ctx.getSender(), meta.permission()));
        builder.executes(this::preExecute);
        com.tty.api.annotations.ArgumentCommand annotation = this.getClass().getAnnotation(com.tty.api.annotations.ArgumentCommand.class);
        if (annotation != null && annotation.isSuggests()) {
            builder.suggests((ctx, b) -> {
                String input = ctx.getInput().trim().replaceFirst("/", "");
                for (String name : PLUGIN_NAMES) {
                    if (input.startsWith(name + " ")) {
                        input = input.substring(name.length() + 1).trim();
                        break;
                    }
                }
                String[] args = input.isEmpty() ? new String[0] : input.split(" ");
                CompletableFuture<Set<String>> tabbed = this.tabSuggestions(ctx.getSource().getSender(), args);
                if (tabbed == null) return b.buildFuture();
                return tabbed.thenApply(list -> {
                    for (String s : list) {  b.suggest(s); }
                    return b.build();
                });
            });
        }
        for (SuperHandsomeCommand command : this.thenCommands()) {
            builder.then(command.toBrigadier());
        }

        return builder.build();
    }

    @Override
    public int preExecute(CommandContext<CommandSourceStack> ctx) {

        CommandMeta meta = this.getClass().getAnnotation(CommandMeta.class);
        CommandSender sender = ctx.getSource().getSender();

        if (!meta.allowConsole() && !(sender instanceof Player)) {
            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("function.public.not-player")));
            return 0;
        }

        String input = ctx.getInput().trim();

        for (String name : PLUGIN_NAMES) {
            if (input.startsWith(name + " ")) {
                input = input.substring(name.length()).trim();
                break;
            }
        }

        String[] args = input.isEmpty() ? new String[0] : input.split(" ");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("\"") && arg.endsWith("\"") && arg.length() >= 2) {
                args[i] = arg.substring(1, arg.length() - 1);
            }
        }

        if (args.length != meta.tokenLength()) {
            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("function.public.fail")));
            return 0;
        }

        this.execute(sender, args);

        return 1;
    }

    protected abstract @NotNull ArgumentType<T> argumentType();

    public abstract CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args);

}
