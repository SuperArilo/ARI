package com.tty.commands.sub;

import com.tty.Ari;
import com.tty.lib.dto.event.CustomPluginReloadEvent;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.tool.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Reload extends BaseLiteralArgumentLiteralCommand {

    public Reload() {
        super(true, 1, true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("function.reload.doing")));
        Bukkit.getPluginManager().callEvent(new CustomPluginReloadEvent(sender));
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String permission() {
        return "ari.command.reload";
    }
}
