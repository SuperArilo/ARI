package com.tty.ari.commands.sub;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.gui.EnderChestEdit;
import com.tty.ari.states.gui.GuiManagerStateService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandMeta(displayName = "name or uuid (string)", permission = "ari.command.enderchest.player", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class EnderChestToPlayer extends RequiredArgumentCommand<String> {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (args.length < 2) return 0;
        Player player = (Player) sender;
        UUID uuid = PublicFunctionUtils.parseUUID(args[1]);
        if (uuid == null) {
            sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return 0;
        }
        Ari.REPOSITORY_MANAGER.get(ServerPlayer.class)
            .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid.toString()), PartitionKey.global())
            .thenCompose(serverPlayer -> CompletableFuture.completedFuture(serverPlayer != null))
            .thenAccept(status -> {
                if (!status) {
                    sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                    return;
                }
                OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
                if (offlinePlayer instanceof Player p) {
                    player.openInventory(p.getEnderChest());
                    return;
                }
                Ari.STATE_MACHINE_MANAGER.get(GuiManagerStateService.class).addState(new OnCheckPlayerGuiState(player, offlinePlayer, new EnderChestEdit(player, offlinePlayer)));
            });
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        Set<String> strings = onlinePlayers.stream().map(Player::getName).collect(Collectors.toSet());
        if (onlinePlayers.isEmpty() || args.length != 3) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[2], strings));
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
