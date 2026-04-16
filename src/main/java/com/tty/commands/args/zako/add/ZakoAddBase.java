package com.tty.commands.args.zako.add;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.command.RequiredArgumentCommand;
import com.tty.entity.WhitelistInstance;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ZakoAddBase<T> extends RequiredArgumentCommand<T> {

    protected void addPlayer(CommandSender sender, String[] args) {
        String value = args[2];
        UUID uuid = PublicFunctionUtils.parseUUID(value);
        if (uuid == null) return;

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
                        sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-error")));
                    }
                });
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
