package com.tty.commands.sub.tpa;


import com.tty.Ari;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.enumType.FilePath;
import com.tty.lib.command.BaseRequiredArgumentLiteralCommand;
import com.tty.lib.enum_type.TeleportType;
import com.tty.states.teleport.PreTeleportStateService;
import com.tty.tool.ConfigUtils;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class TpaBaseLiteralLiteralArgument extends BaseRequiredArgumentLiteralCommand<PlayerSelectorArgumentResolver> {


    public TpaBaseLiteralLiteralArgument(Integer correctArgsLength) {
        super(false, correctArgsLength, ArgumentTypes.player(), true);
    }

    /**
     * 获取适合 tpa 的玩家列表
     * @param sender 发起者
     * @return 返回玩家名称列表
     */
    public Set<String> getOnlinePlayers(CommandSender sender) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equals(sender.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * 接收者获取它获取到的列表
     * @param sender 接收者
     * @return 能够执行的列表
     */
    public Set<String> getResponseList(CommandSender sender) {
        return Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class).getList().stream()
                .filter(i -> i.getTarget().equals(sender))
                .filter(i -> i.getType().equals(TeleportType.TPA))
                .map(e -> e.getOwner().getName())
                .collect(Collectors.toSet());
    }

    /**
     * 检查执行逻辑
     * @param sender 接收者
     * @param target 发起者
     */
    public void checkAfterResponse(Player sender, Player target, Consumer<PreEntityToEntityState> consumer) {
        if (target == null) {
            ConfigUtils.t("teleport.unable-player", sender).thenAccept(sender::sendMessage);
            return;
        }
        PreTeleportStateService machine = Ari.STATE_MACHINE_MANAGER.get(PreTeleportStateService.class);
        //检查这个请求是否存在
        PreEntityToEntityState anElse = machine
                .getStates(target)
                .stream()
                .filter(i -> i instanceof PreEntityToEntityState state && state.getTarget().equals(sender)).findFirst().orElse(null);
        if (anElse == null) {
            ConfigUtils.t("function.tpa.been-done", sender).thenAccept(sender::sendMessage);
            return;
        }
        consumer.accept(anElse);
        //移除发起者的请求
        machine.removeState(anElse);
    }


    public boolean preCheckIsNotPass(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.TPA_CONFIG.name()))) return true;
        Player player = Ari.instance.getServer().getPlayerExact(args[1]);
        if (player == null || sender.getName().equals(player.getName())) {
            ConfigUtils.t("teleport.unable-player", (Player) sender).thenAccept(sender::sendMessage);
            return true;
        }
        return false;
    }
}
