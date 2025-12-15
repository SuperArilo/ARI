package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.enumType.FilePath;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.TeleportType;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.states.teleport.PreTeleportStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaHereArgs extends TpaBaseLiteralLiteralArgument {

    public TpaHereArgs() {
        super(2);
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        List<String> strings = this.getOnlinePlayers(sender);
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
        return "ari.command.tpahere";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (this.preCheckIsNotPass(sender, args)) return;

        Player owner = (Player) sender;
        Player player = Ari.instance.getServer().getPlayerExact(args[1]);

        Ari.instance.stateMachineManager
                .get(PreTeleportStateService.class)
                .addState(new PreEntityToEntityState(
                        owner,
                        player,
                        TeleportType.TPAHERE,
                        Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.TPA_CONFIG, Integer.class, 10)
                ));
    }
}
