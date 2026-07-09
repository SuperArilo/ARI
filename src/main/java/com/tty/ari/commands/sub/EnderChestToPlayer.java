package com.tty.ari.commands.sub;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import com.tty.ari.entity.ServerPlayer;
import com.tty.ari.gui.EnderChestEdit;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);

        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return 0;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        Ari.REPOSITORY_MANAGER.get(ServerPlayer.class)
            .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid.toString()), PartitionKey.global())
            .thenCompose(serverPlayer -> CompletableFuture.completedFuture(serverPlayer != null))
            .thenAccept(status -> {
                if (!status) {
                    sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                    return;
                }
                if (offlinePlayer instanceof Player p) {
                    player.openInventory(p.getEnderChest());
                    return;
                }
                Ari.instance.getStatusManager().get(GuiManagerStateService.class).addState(new OnCheckPlayerGuiState(player, offlinePlayer, new EnderChestEdit(player, offlinePlayer)));
            });
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(this.getExcludeMePlayerList(sender, args));
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
