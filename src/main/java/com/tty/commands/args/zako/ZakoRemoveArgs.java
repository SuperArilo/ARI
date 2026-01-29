package com.tty.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.WhitelistInstance;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.annotations.ArgumentCommand;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name | uuid (string)", permission = "ari.command.zako.remove", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoRemoveArgs extends BaseRequiredArgumentLiteralCommand<String> {

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
        String value = args[2];
        UUID uuid = PublicFunctionUtils.parseUUID(value);
        if (uuid == null) return;
        EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        repository.get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString())).thenCompose(instance -> {
            if (instance == null) {
                return CompletableFuture.completedFuture(false);
            }
            return repository.delete(instance);
        }).thenAccept(status -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                Lib.Scheduler.runAtEntity(Ari.instance,
                        player, i->
                                player.kick(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-player.data-changed"))), null);
            }
            String key = "function.zako.whitelist-remove-" + (status ? "success":"failure");
            if (sender instanceof Player p) {
                ConfigUtils.t(key, p).thenAccept(sender::sendMessage);
            } else {
                ConfigUtils.t(key).thenAccept(sender::sendMessage);
            }
        }).exceptionally(i -> {
            Log.error(i);
            sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-error")));
            return null;
        });
    }
}
