package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoInfoPlayer;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ZakoInfo extends BaseLiteralArgumentLiteralCommand {

    public ZakoInfo() {
        super(true, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoInfoPlayer());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String permission() {
        return "ari.command.zako.info";
    }
}
