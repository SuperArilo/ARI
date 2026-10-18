package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.WeatherArgs;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "weather", permission = "ari.command.weather", tokenLength = 1)
public class Weather extends LiteralArgumentCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new WeatherArgs());
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
