package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.dto.state.teleport.PlayerToPlayerState;
import com.tty.enumType.FilePath;
import com.tty.api.annotations.ArgumentCommand;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.PublicFunctionUtils;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
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
            assert target != null;
            int value = Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.TPA_CONFIG, Integer.class, 3);
            PlayerToPlayerState state;
            if (s.getType().getKey().equals("tpa")) {
                state = new PlayerToPlayerState(target, player, value, "tpa");
            } else {
                state = new PlayerToPlayerState(player, target, value, "tpahere");
            }

            //添加传送请求
            if (Ari.STATE_MACHINE_MANAGER.get(TeleportStateService.class).addState(state)) {
                ConfigUtils.t("function.tpa.agree", player).thenAccept(player::sendMessage);
            }
        });
    }
}
