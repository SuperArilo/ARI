package com.tty.commands.args.zako;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tty.Ari;
import com.tty.entity.WhitelistInstance;
import com.tty.lib.Log;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.Operator;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ZakoAddArgs extends ZakoBaseArgs<String> {

    public ZakoAddArgs() {
        super(true, 3, StringArgumentType.string(), false);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "name | uuid (string)";
    }

    @Override
    public String permission() {
        return "ari.command.zako.add";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String value = args[2];
        UUID uuid = this.parseUUID(value);
        if (uuid == null) return;

        this.whitelistManager.getInstance(uuid.toString())
            .thenCompose(s -> {
                if (s != null) {
                    sender.sendMessage(ConfigUtils.t("function.zako.player-exist"));
                    return CompletableFuture.completedFuture(false);
                }
                return CompletableFuture.completedFuture(true);
            })
            .thenCompose(i -> {
                if (!i) return CompletableFuture.completedFuture(null);

                WhitelistInstance instance = new WhitelistInstance();
                instance.setPlayerUUID(uuid.toString());
                instance.setAddTime(System.currentTimeMillis());
                instance.setOperator(Operator.getOperator(sender).toString());

                return this.whitelistManager.createInstance(instance);
            })
            .thenAccept(status -> {
                if (status == null) return;
                sender.sendMessage(ConfigUtils.t("function.zako.add-" + (status ? "success":"failure")));
            })
            .whenComplete((v, ex) -> {
                if (ex != null) {
                    Log.error(ex, "add zako error.");
                    sender.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-error")));
                }
            });
    }
}
