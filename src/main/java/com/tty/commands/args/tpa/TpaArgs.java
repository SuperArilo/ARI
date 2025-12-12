package com.tty.commands.args.tpa;

import com.tty.Ari;
import com.tty.commands.sub.tpa.TpaBaseLiteralLiteralArgument;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.enumType.FilePath;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.enum_type.TeleportType;
import com.tty.states.teleport.PreTeleportStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaArgs extends TpaBaseLiteralLiteralArgument {

    public TpaArgs(String name, String permission) {
        super(name, permission);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String permission() {
        return this.permission;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.isDisabledInGame(sender, Ari.C_INSTANCE.getObject(FilePath.TPA_CONFIG.name()))) return;

        Player owner = (Player) sender;
        Player player = Ari.instance.getServer().getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage(ConfigUtils.t("teleport.unable-player"));
            return;
        }
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
    public List<String> tabSuggestions() {
        return List.of();
    }
}
