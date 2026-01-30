package com.tty.commands.args.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.command.RequiredArgumentCommand;
import com.tty.entity.WhitelistInstance;
import com.tty.api.Log;
import com.tty.api.annotations.ArgumentCommand;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.PublicFunctionUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name | uuid (string)", permission = "ari.command.zako.add", tokenLength = 3, allowConsole = true)
@ArgumentCommand
public class ZakoAddArgs extends RequiredArgumentCommand<String> {

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
        repository.get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString()))
            .thenCompose(s -> {
                if (s != null) {
                    CompletableFuture<Component> msgFuture =
                            (sender instanceof Player player)
                                    ? ConfigUtils.t("function.zako.zako-add-exist", player)
                                    : ConfigUtils.t("function.zako.zako-add-exist");

                    return msgFuture
                            .thenAccept(sender::sendMessage)
                            .thenApply(v -> false);
                }
                return CompletableFuture.completedFuture(true);
            })
            .thenCompose(i -> {
                if (!i) return CompletableFuture.completedFuture(null);

                WhitelistInstance instance = new WhitelistInstance();
                instance.setPlayerUUID(uuid.toString());
                instance.setAddTime(System.currentTimeMillis());
                instance.setOperator(Operator.getOperator(sender).toString());

                return repository.create(instance);
            })
            .thenAccept(status -> {
                if (status == null) return;
                CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.zako-add-success", player):ConfigUtils.t("function.zako.zako-add-success");
                future.thenAccept(sender::sendMessage);
            })
            .whenComplete((v, ex) -> {
                if (ex != null) {
                    Log.error(ex, "add zako error.");
                    sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-error")));
                }
            });
    }
}
