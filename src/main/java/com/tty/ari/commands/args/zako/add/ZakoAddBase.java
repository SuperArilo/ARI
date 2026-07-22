package com.tty.ari.commands.args.zako.add;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.ComponentTool;
import com.tty.api.enumType.Operator;
import com.tty.api.repository.EntityRepository;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.RequiredArgumentCommand;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ZakoAddBase<T> extends RequiredArgumentCommand<T> {

    protected void addPlayer(CommandSender sender, String[] args) {

        OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[2]);
        if (offlinePlayer == null) {
            ConfigUtils.t("function.zako.zako-add-name-invalid").thenAccept(sender::sendMessage);
            return;
        }

        UUID uuid = offlinePlayer.getUniqueId();

        EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        repository.get(new LambdaQueryWrapper<>(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString()), PartitionKey.global()).thenCompose(s -> {
            if (s != null) {
                return ConfigUtils.t("function.zako.zako-add-exist").thenAccept(sender::sendMessage).thenApply(v -> false);
            }
            return CompletableFuture.completedFuture(true);
        }).thenCompose(i -> {
            if (!i) return CompletableFuture.completedFuture(null);

            WhitelistInstance instance = new WhitelistInstance();
            instance.setPlayerUUID(uuid.toString());
            instance.setAddTime(System.currentTimeMillis());
            instance.setOperator(Operator.getOperator(sender).toString());
            if(args.length == 4) {
                instance.setRemark(args[3]);
            }
            return repository.create(instance, PartitionKey.global());
        }).thenAccept(status -> {
            if (status == null) return;
            ConfigUtils.t("function.zako.zako-add-success").thenAccept(sender::sendMessage);
        }).whenComplete((v, ex) -> {
            if (ex != null) {
                Ari.instance.getLog().error(ex);
                sender.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-error")));
            }
        });
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
