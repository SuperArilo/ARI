package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.dto.state.teleport.PlayerToPlayerState;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.enumType.FilePath;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.states.teleport.TeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaAcceptArgs extends TpaBaseLiteralLiteralArgument {

    public TpaAcceptArgs() {
        super(2);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        List<String> strings = this.getResponseList(sender);
        if (args.length != 2) return strings;
        return PublicFunctionUtils.tabList(args[1], strings);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return "name (string)";
    }

    @Override
    public String permission() {
        return "ari.command.tpaaccept";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(this.preCheckIsNotPass(sender, args)) return;

        Player player = (Player) sender;
        Player target = Ari.instance.getServer().getPlayerExact(args[1]);

        PreEntityToEntityState anElse = this.checkAfterResponse(player, target);
        if (anElse == null) return;

        assert target != null;
        int value = Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.TPA_CONFIG, Integer.class, 3);
        PlayerToPlayerState state;
        if (anElse.getType().getKey().equals("tpa")) {
            state = new PlayerToPlayerState(target, player, value, "tpa");
        } else {
            state = new PlayerToPlayerState(player, target, value, "tpahere");
        }

        //添加传送请求
        if (Ari.instance.stateMachineManager.get(TeleportStateService.class).addState(state)) {
            player.sendMessage(ConfigUtils.t("function.tpa.agree"));
        }
    }
}
