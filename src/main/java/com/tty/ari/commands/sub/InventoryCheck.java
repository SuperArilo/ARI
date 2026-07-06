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
import com.tty.ari.gui.PlayerInventoryEdit;
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

@CommandMeta(displayName = "name or uuid (string)", permission = "ari.command.inv", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class InventoryCheck extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
    }

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        return CompletableFuture.completedFuture(this.getExcludeMePlayerList(sender, args));
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (args.length < 2 || !(sender instanceof Player player)) return 0;

        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);

        if (offlinePlayer == null) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return 0;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        if (player.getUniqueId().equals(uuid)) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.command.self-not-allowed")));
            return 0;
        }

        Ari.REPOSITORY_MANAGER.get(ServerPlayer.class)
                .get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid.toString()), PartitionKey.global())
                .thenCompose(serverPlayer -> CompletableFuture.completedFuture(serverPlayer != null))
                .thenAccept(status -> {
                    if (!status) {
                        sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
                        return;
                    }

                    GuiManagerStateService service = Ari.instance.getStatusManager().get(GuiManagerStateService.class);

                    if (service.getStates(player).stream().anyMatch(i -> (i instanceof OnCheckPlayerGuiState state && state.getOwner().equals(player)))) {
                        sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.task-occupied")));
                        return;
                    }
                    service.addState(new OnCheckPlayerGuiState(player, offlinePlayer, new PlayerInventoryEdit(Ari.instance, player, offlinePlayer)));
                }).exceptionally(e -> {
                    Ari.instance.getLog().error(e);
                    sender.sendMessage(this.getPlugin().getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error")));
                   return null;
                });
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
