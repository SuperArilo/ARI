package com.tty.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.tty.Ari;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public abstract class LiteralArgumentCommand extends PreCommand {

    @Override
    public int preExecute(CommandContext<CommandSourceStack> ctx) {

        CommandMeta meta = this.getClass().getAnnotation(CommandMeta.class);
        CommandSender sender = ctx.getSource().getSender();

        if (!meta.allowConsole() && !(sender instanceof Player)) {
            sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("function.public.not-player")));
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
            sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("function.public.fail")));
            return 0;
        }

        this.execute(sender, args);

        return 1;
    }

    @Override
    public CommandNode<CommandSourceStack> toBrigadier() {
        CommandMeta meta = this.getClass().getAnnotation(CommandMeta.class);
        if (meta == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " lost @CommandMeta");
        }
        LiteralArgumentBuilder<CommandSourceStack> top_mian = Commands.literal(meta.displayName());
        top_mian.requires(ctx -> Ari.PERMISSION_SERVICE.hasPermission(ctx.getSender(), meta.permission()));
        LiteralCommand annotation = this.getClass().getAnnotation(LiteralCommand.class);
        if (annotation != null && annotation.directExecute()) {
            top_mian.executes(this::preExecute);
        }
        for (SuperHandsomeCommand subCommand : this.thenCommands()) {
            top_mian.then(subCommand.toBrigadier());
        }
        return top_mian.build();
    }

}
