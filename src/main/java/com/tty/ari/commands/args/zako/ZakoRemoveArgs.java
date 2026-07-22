package com.tty.ari.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.api.ComponentTool;
import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name | uuid (string)", permission = "ari.command.zako.remove", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoRemoveArgs extends RequiredArgumentCommand<String> {

    @Override
    protected @NotNull ArgumentType<String> argumentType() {
        return StringArgumentType.string();
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

        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);

        if (offlinePlayer == null) {
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.not-exist")));
            return;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        LambdaQueryWrapper<WhitelistInstance> wrapper = new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString());
        repository.delete(wrapper, PartitionKey.global()).thenAccept(count -> {
            if (offlinePlayer instanceof Player player) {
                Ari.instance.getScheduler().runAtEntity(player, i-> player.kick(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed"))), null);
            }
            String key = "function.zako.whitelist-remove-" + (count == 1 ? "success":"failure");
            if (sender instanceof Player p) {
                ConfigUtils.t(key, p).thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t(key).thenAccept(sender::sendMessage);
            }
        }).exceptionally(i -> {
            Ari.instance.getLog().error(i);
            sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-error")));
            return null;
        });

    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
