package com.tty.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.tty.Ari;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;


@SuppressWarnings("UnstableApiUsage")
public abstract class RequiredArgumentCommand<T> extends PreCommand {

    @Override
    public CommandNode<CommandSourceStack> toBrigadier() {
        CommandMeta meta = this.getClass().getAnnotation(CommandMeta.class);
        if (meta == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " lost @CommandMeta");
        }
        RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(meta.displayName(), this.argumentType());
        builder.requires(ctx -> Ari.PERMISSION_SERVICE.hasPermission(ctx.getSender(), meta.permission()));
        builder.executes(this::preExecute);
        com.tty.api.annotations.command.ArgumentCommand annotation = this.getClass().getAnnotation(com.tty.api.annotations.command.ArgumentCommand.class);
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

    protected abstract @NotNull ArgumentType<T> argumentType();

    public abstract CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args);

}
