package com.tty.ari.commands.args;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.dto.state.player.PlayerMorphState;
import com.tty.ari.states.PlayerMorphService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandMeta(displayName = "entity_id (string)", permission = "ari.command.morph", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class MorphArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> collect = Arrays.stream(EntityType.values()).filter(i -> i.isSpawnable() && i.isAlive()).map(i -> i.name().toLowerCase()).collect(Collectors.toSet());
        if (args.length == 1) return CompletableFuture.completedFuture(collect);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], collect));
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return 0;
        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase());
        } catch (Exception e) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.input-error"), player));
            return 0;
        }
        Ari.instance.getStatusManager().get(PlayerMorphService.class).addState(new PlayerMorphState(player, type));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
