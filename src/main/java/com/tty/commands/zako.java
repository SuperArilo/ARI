package com.tty.commands;

import com.tty.commands.sub.zako.*;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class zako extends BaseLiteralArgumentLiteralCommand {

    public zako() {
        super(false, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoAdd(),
                new ZakoList(),
                new ZakoInfo(),
                new ZakoBan(),
                new ZakoUnBan(),
                new ZakoRemove());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    public String name() {
        return "zako";
    }

    @Override
    public String permission() {
        return "ari.command.zako";
    }
}
