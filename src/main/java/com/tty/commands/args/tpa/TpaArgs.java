package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.enumType.FilePath;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.TeleportType;
import com.tty.states.teleport.PreTeleportStateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaArgs extends TpaBaseLiteralLiteralArgument {

    public TpaArgs() {
        super(false, 2);
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
        return "ari.command.tpa";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.preCheck(sender, args)) return;
        Player owner = (Player) sender;
        Player player = Ari.instance.getServer().getPlayerExact(args[1]);
        Ari.instance.stateMachineManager
                .get(PreTeleportStateService.class)
                .addState(new PreEntityToEntityState(
                        owner,
                        player,
                        TeleportType.TPA,
                        Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.TPA_CONFIG, Integer.class, 10)
                ));
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender) {
        return this.getOnlinePlayers(sender);
    }
}
