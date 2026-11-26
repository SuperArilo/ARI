package com.tty.ari.commands.args.zako.add;

import com.tty.api.enumType.Operator;
import com.tty.api.persistence.query.Query;
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

        Ari.REPOSITORY_MANAGER.whitelist().findOne(Query.of(WhitelistInstance.class).eq(WhitelistInstance::getPlayerUUID, uuid.toString())).thenCompose(s -> {
            if (s.isPresent()) {
                return ConfigUtils.t("function.zako.zako-add-exist").thenAccept(sender::sendMessage).thenApply(v -> (WhitelistInstance) null);
            }

            WhitelistInstance instance = new WhitelistInstance();
            instance.setPlayerUUID(uuid.toString());
            instance.setAddTime(System.currentTimeMillis());
            instance.setOperator(Operator.getOperator(sender).toString());
            if(args.length == 4) {
                instance.setRemark(args[3]);
            }
            return Ari.REPOSITORY_MANAGER.whitelist().insert(instance);
        }).thenAccept(status -> {
            if (status == null) return;
            ConfigUtils.t("function.zako.zako-add-success").thenAccept(sender::sendMessage);
        }).whenComplete((v, ex) -> {
            if (ex != null) {
                Ari.instance.getLog().error(ex);
                sender.sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-error")));
            }
        });
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
