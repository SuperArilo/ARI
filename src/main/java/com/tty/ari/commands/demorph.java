package com.tty.ari.commands;

import com.mojang.brigadier.Command;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.dto.state.player.PlayerMorphState;
import com.tty.ari.states.PlayerMorphService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandMeta(displayName = "demorph", permission = "ari.command.demorph", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class demorph extends LiteralArgumentCommand {

    @Override
    public int execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return 0;
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.require-pre-plugin"), player));
            return 0;
        }
        for (PlayerMorphState state : Ari.instance.getStatusManager().get(PlayerMorphService.class).getStates(player)) {
            state.setOver(true);
        }
        ConfigUtils.t("function.morph.restore", player).thenAccept(sender::sendMessage);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
