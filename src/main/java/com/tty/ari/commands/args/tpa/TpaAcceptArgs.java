package com.tty.ari.commands.args.tpa;

import com.tty.api.annotations.command.ArgumentCommand;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.ari.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.dto.state.teleport.PlayerToPlayerState;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.teleport.TeleportStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "name (string)", permission = "ari.command.tpaaccept", tokenLength = 2)
@ArgumentCommand(isSuggests = true)
public class TpaAcceptArgs extends TpaBaseLiteralLiteralArgument {

    @Override
    public CompletableFuture<Set<String>> tabSuggestions(CommandSender sender, String[] args) {
        Set<String> strings = this.getResponseList(sender);
        if (args.length != 2) return CompletableFuture.completedFuture(strings);
        return CompletableFuture.completedFuture(PublicFunctionUtils.tabList(args[1], strings));
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(this.preCheckIsNotPass(sender, args)) return;

        Player player = (Player) sender;
        Player target = Ari.instance.getServer().getPlayerExact(args[1]);
        this.checkAfterResponse(player, target, s -> {

            int value = Ari.instance.getConfigurationManager().get(FunctionConfig.class).getTeleportDelay(TeleportType.TPA);
            PlayerToPlayerState state;
            if (s.getType().getKey().equals("tpa")) {
                state = new PlayerToPlayerState(target, player, value, "tpa");
            } else {
                state = new PlayerToPlayerState(player, target, value, "tpahere");
            }

            //添加传送请求
            Ari.instance.getStatusManager().get(TeleportStateService.class).addState(state);
            ConfigUtils.t("function.tpa.agree", player).thenAccept(player::sendMessage);
        });
    }
}
