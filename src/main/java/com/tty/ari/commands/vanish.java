package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.state.State;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.states.PlayerVanishService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "vanish", permission = "ari.command.vanish", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class vanish extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        PlayerVanishService service = Ari.instance.getStatusManager().get(PlayerVanishService.class);
        if (service.isNotHaveState(player)) {
            service.addState(new State(player, Integer.MAX_VALUE));
        } else {
            service.getStates(player).forEach(i -> i.setOver(true));
        }
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return Ari.instance.getConfigurationManager().get(FunctionConfig.class).vanishIsEnable();
    }

}
