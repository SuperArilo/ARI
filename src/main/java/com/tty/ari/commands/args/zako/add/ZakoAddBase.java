package com.tty.ari.commands.args.zako.add;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.tty.ari.Ari;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ZakoAddBase<T> extends RequiredArgumentCommand<T> {

    protected int addPlayer(CommandSender sender, String[] args) {
        String value = args[2];
        if (!this.isValidPlayerName(value)) {
            ConfigUtils.t("function.zako.zako-add-name-invalid").thenAccept(sender::sendMessage);
            return 0;
        }
        UUID uuid = PublicFunctionUtils.parseUUID(value);

        EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        repository.get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString()), PartitionKey.global())
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
                    if(args.length == 4) {
                        instance.setRemark(args[3]);
                    }

                    return repository.create(instance, PartitionKey.global());
                })
                .thenAccept(status -> {
                    if (status == null) return;
                    CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.zako-add-success", player):ConfigUtils.t("function.zako.zako-add-success");
                    future.thenAccept(sender::sendMessage);
                })
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        Ari.instance.getLog().error(ex, "add zako error.");
                        sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error")));
                    }
                });
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

    protected boolean isValidPlayerName(String name) {
        if (name == null) {
            return false;
        }
        return name.matches("^[a-zA-Z0-9_]{3,16}$");
    }

}
